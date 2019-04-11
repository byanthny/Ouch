package com.sim.ouch.web

import com.sim.ouch.Slogger
import com.sim.ouch.logic.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.websocket.*
import kotlinx.coroutines.*
import java.util.function.Consumer

private const val IDLE_TIMOUT_MS = 1_000L * 60

val ER_EX_NOT_FOUND = 4004 to "existence not found"
val ER_NO_NAME = 4005 to "no name"
val ER_DUPLICATE_NAME = 4006 to "duplicate name"
val ER_BAD_TOKEN = 4007 to "invalid token"
val ER_INTERNAL = 4010 to "internal err"
val ER_Q_NOT_FOUND = 4040 to "quididty not found"

private val sl = Slogger("Socket Handler")

/** Server side [WsHandler] implementation. */
val Websocket = Consumer<WsHandler> { wsHandler ->

    val connect = ConnectHandler ch@{ session ->
        // Check for reconnection token
        session.queryParam("token")?.also { token ->
            // Attempt to validate the token
            runBlocking { DAO.refreshSession(token, session) }?.also { (_, sd) ->
                // Attempt to locate existence
                val ex = runBlocking { DAO.getExistence(sd.ec) }
                    ?: return@ch session.close(ER_EX_NOT_FOUND)
                val q = ex.qOf(sd.qc)
                    ?: return@ch session.close(ER_Q_NOT_FOUND)
                session.idleTimeout = IDLE_TIMOUT_MS
                return@ch session.initWith(ex, q, token)
                    .also { ex.broadcast(ENTER, q, false, session.id) }
                    .also { sl.slog("Init reconnect session. Ex=${ex._id} SID=${session.id}") }
            } ?: return@ch session.close(ER_BAD_TOKEN)
        }

        // Standard Start Connection
        lateinit var t: Token
        lateinit var qd: Quiddity
        val name = session.queryParam("name")
            ?: return@ch session.close(ER_NO_NAME)
        val ex = session.queryParam("exID")?.let { ec ->
            runBlocking { DAO.getExistence(ec) }?.also { e ->
                qd = e.qOfName(name) // ?.also { TODO("Check for duplicates") }
                    ?: e.generateQuidity(name)
                t = runBlocking { DAO.addSession(session, e, qd) }
                    ?: return@ch session.close(ER_INTERNAL)
            } ?: return@ch session.close(ER_EX_NOT_FOUND)
        } ?: run {
            runBlocking { DAO.addExistence(session, DefaultExistence(), name) }
                ?.let {
                    t = it.first
                    qd = it.third
                    it.second
                } ?: return@ch session.close(ER_INTERNAL)
        }
        session.idleTimeout = IDLE_TIMOUT_MS
        session.initWith(ex, qd, t)
            .also { sl.slog("Init session. Ex=${ex._id} SID=${session.id}") }
            .also { ex.broadcast(ENTER, qd, false, session.id) }
    }

    val message = MessageHandler { session, msg ->
        val sd = runBlocking { DAO.getSessionData(session) }
        val packet = readPacket(msg)
        val ex = sd?.let { runBlocking { DAO.getExistence(it.ec) } }
        val qd = sd?.let { ex?.qOf(it.qc) }
        when (packet.dataType) {
            ACTION -> TODO()
            CHAT -> {
                if (ex == null || qd == null) sl.elog("Failed to handle chat.")
                else handleChat(ex, qd, session, msg, packet.data as String)
            }
            PING -> session.send(pack(PING, "pong"))
            else -> session.send("Client cannot make ${packet.dataType} requests.")
        }
    }

    suspend fun close(session: WsSession) = session.let {
        val qc = DAO.getSessionData(session)?.qc
        it.existence()?.broadcast(EXIT, qc ?: "", true)
        DAO.disconnect(it)
    }

    wsHandler.onClose { session, code, reason ->
        sl.slog("Close session. $code \"$reason\"")
        launch { close(session) }
    }

    val err = ErrorHandler { session, t: Throwable? ->
        if (session.isOpen) session.send(Packet(INTERNAL, t?.message, true).pack())
        else launch { close(session) }
    }

    wsHandler.onConnect(connect)
    wsHandler.onMessage(message)
    wsHandler.onError(err)
}

/** Handle chat broadcasting and parsing. */
private fun handleChat(
    ex: Existence, qd: Quiddity, ses: WsSession, msg: String, text: String
) {
    // Update chat
    ex.chat.update(qd, text).let { m -> ex.broadcast(chatPacket(m)) }
    // Parse for keywords
    if (qd.ouch.add(text.parseOof)) ex.broadcast(QUIDDITY, qd)
    // Save existence
    if (!runBlocking { DAO.saveExistence(ex) })
        sl.elog("Failed to update Existence after chat")
}

private fun handleAction(ex: Existence, qd: Quiddity, ses: WsSession, action: Action) {
    // TODO
}

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)

val handler = CoroutineExceptionHandler { _, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}

fun launch(block: suspend CoroutineScope.() -> Unit) =
        GlobalScope.launch(handler, block = block)
