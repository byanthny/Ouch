package com.sim.ouch.web

import com.sim.ouch.ER_BAD_ID
import com.sim.ouch.ER_NO_NAME
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.UnauthorizedResponse
import io.javalin.websocket.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.function.Consumer

private const val IDLE_TIMOUT = 1_020_000L

/**
 * Server side [WsHandler] implementation
 */
val Websocket = Consumer<WsHandler> {
    val connect = ConnectHandler { session ->
        session.idleTimeout = IDLE_TIMOUT
        launch {
            // Check for reconnection key
            TODO()
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
                CHAT -> TODO()
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
            TODO("CLOSE ACTION")
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
