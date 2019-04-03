package com.sim.ouch.web

import com.sim.ouch.*
import com.sim.ouch.logic.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.UnauthorizedResponse
import io.javalin.websocket.*
import kotlinx.coroutines.*
import java.util.function.Consumer

/**
 * Server side [WsHandler] implementation
 */
val Websocket = Consumer<WsHandler> {
    val connect = ConnectHandler { session ->
        launch {
            // Check for reconnection key
            session.remoteAddress.hashCode()
        }
    }

    val message = MessageHandler { session, msg ->
        launch {
            val packet = readPacket(msg)
            val ex = DAO.getExistence(session.id)
            when (packet.dataType) {
                QUIDITY -> TODO()
                EXISTENCE -> TODO()
                ACTION -> TODO()
                CHAT -> ex?.chat?.update(
                    session.quidity()!!, packet.data as String
                )?.let { m -> ex.sessions?.broadcast(CHAT, m) }
                PING -> session.send(Packet(PING, "pong").pack())
                else -> throw UnauthorizedResponse(
                    "Client cannot make ${packet.dataType} requests.")
            }
        }
    }

    val close = CloseHandler { session, statusCode, reason ->
        launch {
            when (statusCode) {
                ER_NO_NAME.first -> println("no name")
                ER_BAD_ID.first -> println("bad _id")
                else -> println("close on $statusCode=$reason")
            }
            session.quidity()?.also {
                session.existence()?.sessions?.broadcast(EXIT, it, session.id)
            }
            DAO.removeSession(session)
        }
    }

    val err = ErrorHandler { session, throwable: Throwable? ->
        TODO()
    }

    it.onConnect(connect)
    it.onMessage(message)
    it.onClose(close)
    it.onError(err)

}

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)

val handler = CoroutineExceptionHandler { _, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}

fun launch(block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(handler, block = block)
