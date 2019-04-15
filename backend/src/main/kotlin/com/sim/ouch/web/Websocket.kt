package com.sim.ouch.web

import com.sim.ouch.Slogger
import com.sim.ouch.launch
import com.sim.ouch.logic.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.websocket.*
import kotlinx.coroutines.runBlocking
import java.util.function.Consumer

private const val IDLE_TIMOUT_MS = 1_000L * 60

val ER_EX_NOT_FOUND = 4004 to "existence not found"
val ER_NO_NAME = 4005 to "no name"
val ER_DUPLICATE_NAME = 4006 to "duplicate name"
val ER_BAD_TOKEN = 4007 to "invalid token"
val ER_BAD_PACKET = 4008 to "invalid packet"
val ER_INTERNAL = 4010 to "internal err"
val ER_Q_NOT_FOUND = 4040 to "quididty not found"

private val sl = Slogger("Socket")


/** Server side [WsHandler] implementation. */
val Websocket = Consumer<WsHandler> { ws ->

    val connect = ConnectHandler ch@{}

    ws.onConnect(connect)

    ws.onMessage { session, msg ->
        runBlocking {
            getSessionData(session)?.let { readMessage(it, session, msg) }
                ?: initSession(session, msg)
        }
    }

    ws.onClose { session, code, reason ->
        sl.slog("Close session. $code \"$reason\"")
        launch { close(session) }
    }

    ws.onError { session, t: Throwable? ->
        if (session.isOpen) {
            session.send(Packet(INTERNAL, t?.message, true).pack())
        } else launch { close(session) }
    }
}

private data class InitRequest(val ec: EC?, val token: Token)

/** Attempt to initialize a new session. */
private suspend fun initSession(ses: WsSession, msg: String) {
    val packet = readPacket(msg) ?: return ses.close(ER_BAD_PACKET)
    val (ec, token) = packet.unpack<InitRequest>() ?: return ses.close(
        ER_BAD_PACKET)
    when (packet.dataType) {
        TOKEN_AUTH -> {
            val user = try {
                token.readAuth()
            } catch (e: Exception) {
                return ses.close(ER_BAD_TOKEN)
            } ?: return ses.close(ER_BAD_TOKEN)
            if (ec == null || !user.existences.containsKey(
                    ec)) return ses.close(ER_EX_NOT_FOUND)
            val ex = getExistence(ec) ?: return ses.close(ER_EX_NOT_FOUND)
            val qd = user.existences[ec]?.let(ex::qOf) ?: return ses.close(
                ER_Q_NOT_FOUND)
            addSession(user, ses, ex, qd)?.let { ses.initWith(ex, qd, it) }
                ?: ses.close(ER_INTERNAL)
        }
        TOKEN_RECON -> {
            val (ec, qc) = try {
                token.readReconnect()
            } catch (e: Exception) {
                return ses.close(ER_BAD_TOKEN)
            }

        }
        else -> return ses.close(ER_BAD_PACKET)
    }
}

/** Handle incoming Socket messages. */
private suspend fun readMessage(sd: SessionData, ses: WsSession, msg: String) {
    val packet = readPacket(msg) ?: return ses.close(ER_BAD_PACKET)
    val ex = getExistence(sd.ec)
    val qd = ex?.qOf(sd.qc)
    when (packet.dataType) {
        ACTION -> TODO()
        CHAT -> {
            if (ex == null || qd == null) sl.elog("Failed to handle chat.")
            else handleChat(ex, qd, ses, msg, packet.data as String)
        }
        PING -> ses.send(pack(PING, "pong"))
        else -> ses.send("Client cannot make ${packet.dataType} requests.")
    }
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
    if (!runBlocking { saveExistence(ex) })
        sl.elog("Failed to update Existence after chat")
}

private fun handleAction(ex: Existence, qd: Quiddity, ses: WsSession, action: Action) {
    // TODO
}

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)

suspend fun close(session: WsSession) = session.let {
    val qc = getSessionData(session)?.qc
    it.existence()?.broadcast(EXIT, qc ?: "", true)
    disconnect(it)
}

suspend fun WsSession.existence(): Existence? = getToken(this)
    ?.let { token -> getSessionData(token)?.let { getExistence(it.ec) } }

suspend fun WsSession.quidity(): Quiddity? = getToken(this)
    ?.let { t -> getSessionData(t) }
    ?.let { (ec, qc) -> getExistence(ec)?.qOf(qc) }
