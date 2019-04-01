package com.sim.ouch.web

import com.sim.ouch.*
import com.sim.ouch.logic.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.UnauthorizedResponse
import io.javalin.websocket.*
import kotlinx.coroutines.*

/**
 * Server side [WsHandler] implementation
 */
object Websocket : WsHandler() {

    val connect = ConnectHandler { session -> launch {

        // Attempt to parse name
        val name: String? = session.queryParam("name")
        if (name.isNullOrBlank()) return@launch session.close(ER_NO_NAME)
        lateinit var quidity: Quidity

        // Attempt to get an existence
        val exist = session.queryParam("exID")?.let { id ->
            // With an invalid ID, close
            DAO.getEx(id)?.also {
                quidity = DAO.addSession(session, it, name)
                        ?: return@launch session.close(ER_INTERNAL)
                // Broadcast the join event to all sessions but this one
                it.sessions?.broadcast(ENTER, quidity,
                    session.id)
            } ?: return@launch session.close(ER_BAD_ID)
        } ?: let {
            // with no ID, make new Existence
            DAO.newExistence(session, DefaultExistence(
                Quidity(name)))?.also { quidity = it.initialQuidity }
                    ?: return@launch session.close(ER_INTERNAL)
        }

        session.send(Packet(INIT, InitPacket(exist, quidity)).pack())
    }}

    val message = MessageHandler { session, msg -> launch {
        val packet = readPacket(msg)
        val ex = DAO.getExistence(session.id)
        when (packet.dataType) {
            QUIDITY -> TODO()
            EXISTENCE -> TODO()
            ACTION -> TODO()
            CHAT -> ex?.chat?.update(
                session.quidity()!!, packet.data as String
            )?.let { ex.sessions?.broadcast(CHAT, it) }
            PING -> session.send(Packet(PING, "pong").pack())
            else -> throw UnauthorizedResponse(
                "Client cannot make ${packet.dataType} requests."
            )
        }
    }}

    val close = CloseHandler { session, statusCode, reason -> launch {
        when (statusCode) {
            ER_NO_NAME.first -> println("no name")
            ER_BAD_ID.first -> println("bad _id")
            else -> println("close on $statusCode=$reason")
        }
        session.quidity()?.also {
            session.existence()?.sessions?.broadcast(EXIT, it, session.id)
        }
        DAO.removeSession(session)
    }}

    val err = ErrorHandler { session, throwable: Throwable? ->
        TODO()
    }

    init {
        onConnect(connect)
        onMessage(message)
        onClose(close)
        onError(err)
    }
}

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)

val handler = CoroutineExceptionHandler { ctx, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}

fun launch(block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(handler, block = block)
