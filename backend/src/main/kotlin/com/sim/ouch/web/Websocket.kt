package com.sim.ouch.web

import com.sim.ouch.logic.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.UnauthorizedResponse
import io.javalin.websocket.*
import kotlinx.coroutines.*
import kotlinx.coroutines.launch
import java.util.function.Consumer

private const val IDLE_TIMOUT = 1_020_000L

// TODO Add err codes to specs
val ER_NO_NAME  = 4005 to "no name"
val ER_EX_NOT_FOUND = 4004 to "existence not found"
val ER_Q_NOT_FOUND = 4040 to "quididty not found"
val ER_BAD_TOKEN = 4007 to "invalid token"
val ER_INTERNAL = 4010 to "internal err"

/** Server side [WsHandler] implementation. */
val Websocket = Consumer<WsHandler> { wsHandler ->

    val connect = ConnectHandler { session ->
        launch {
            // Check for reconnection token
            session.queryParam("token")?.also { token ->
                // Attempt to validate the token
                DAO.refreshSession(token, session)?.also { (_, sd) ->
                    // Attempt to locate existence
                    val ex = DAO.getExistence(sd.ec)
                        ?: return@launch session.close(ER_EX_NOT_FOUND)
                    val q  = ex.quidities[sd.qc]
                        ?: return@launch session.close(ER_Q_NOT_FOUND)
                    session.idleTimeout = IDLE_TIMOUT
                    return@launch session.initWith(ex, q, token)
                } ?: return@launch session.close(ER_BAD_TOKEN)
            }

            // Standard Start Connection
            lateinit var t: Token
            lateinit var qd: Quiddity
            val name = session.queryParam("name")
                ?: return@launch session.close(ER_NO_NAME)
            val ex = session.queryParam("exID")?.let { ec ->
                DAO.getExistence(ec)?.also {
                    qd = it.generateQuidity(name)
                    t = DAO.addSession(session, it, qd)
                        ?: return@launch session.close(ER_INTERNAL)
                } ?: return@launch session.close(ER_EX_NOT_FOUND)
            } ?: let {
                DefaultExistence(Quiddity(name))
                    .let { DAO.addExistence(session, it) }
                    ?.let {
                        t = it.first
                        qd = it.third
                        it.second
                    } ?: return@launch session.close(ER_INTERNAL)
            }
            session.idleTimeout = IDLE_TIMOUT
            session.initWith(ex, qd, t)
        }
    }

    val message = MessageHandler { session, msg ->
        launch {
            val sd = DAO.getSessionData(session)
            val packet = readPacket(msg)
            val ex = sd?.let { DAO.getExistence(it.ec) }
            val qd = sd?.let { ex?.quidities?.get(it.qc) }
            when (packet.dataType) {
                QUIDITY -> TODO()
                EXISTENCE -> TODO()
                ACTION -> TODO()
                CHAT -> {
                    qd?.let { ex?.chat?.update(it, packet.data as String) }
                        ?.let { m -> ex?.broadcast(chatPacket(m)) }
                    ex?.also { DAO.saveExistence(it) }
                }
                PING -> session.send(Packet(PING, "pong").pack())
                else -> throw UnauthorizedResponse(
                    "Client cannot make ${packet.dataType} requests.")
            }
        }
    }

    wsHandler.onClose { session, _, _ -> launch { DAO.disconnect(session) } }

    val err = ErrorHandler { session, throwable: Throwable? ->
        if (session.isOpen) session.send(Packet(INTERNAL, "err", true).pack())
        else launch { DAO.disconnect(session) }
    }

    wsHandler.onConnect(connect)
    wsHandler.onMessage(message)
    wsHandler.onError(err)

}

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)

val handler = CoroutineExceptionHandler { _, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}

fun launch(block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(handler, block = block)