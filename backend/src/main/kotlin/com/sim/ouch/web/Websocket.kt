package com.sim.ouch.web

import com.sim.ouch.EC
import com.sim.ouch.Name
import com.sim.ouch.Slogger
import com.sim.ouch.Token
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.logic.action.Action
import com.sim.ouch.logic.action.asAction
import com.sim.ouch.logic.parseOof
import com.sim.ouch.web.PacketType.*
import io.javalin.websocket.*
import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.util.function.Consumer
import kotlin.time.Duration.Companion.minutes

private val IDLE_TIMOUT_MS = 5.minutes

private val sl = Slogger("Socket Handler")

object Err {
    val EX_NOT_FOUND = 4004 to "existence not found"
    val NO_NAME = 4005 to "no name"
    val DUPLICATE_NAME = 4006 to "duplicate name"
    val BAD_TOKEN = 4007 to "invalid token"
    val INTERNAL = 4010 to "internal err"
    val Q_NOT_FOUND = 4040 to "quididty not found"
}

/** Server side [WsHandler] implementation. */
val Websocket = Consumer<WsConfig> { wsHandler ->

    val connect = WsConnectHandler ch@{ ctx: WsConnectContext ->
        // Check for reconnection token
        ctx.queryParam("token")
            ?.let { tkn ->
                GlobalScope.launch {
                    sl.slog("Attempting reconnection")
                    reconnect(ctx, tkn)
                }
            }

        // Standard Start Connection
        val name = ctx.queryParam("name") ?: return@ch ctx.close(Err.NO_NAME)
        val exID = ctx.queryParam("exID")
        GlobalScope.launch {
            sl.slog("Attempting new connection")
            newConnection(ctx, name, exID)
        }
    }

    val message = WsMessageHandler { ctx ->
        GlobalScope.launch {
            handleMessage(
                ctx
            )
        }
    }

    suspend fun WsContext.end() = apply {
        if (existence != null && quiddity != null)
            existence!!.broadcast(packetOf(EXIT, quiddity!!.id))
    }.remove()

    wsHandler.onClose { ctx: WsCloseContext ->
        sl.slog("Close session. ${ctx.status()} \"${ctx.reason()}\"")
        GlobalScope.launch { ctx.end() }
    }

    val err = WsErrorHandler { ctx: WsErrorContext ->
        if (ctx.session.isOpen) ctx.send(pack(INTERNAL, ctx.error()?.message))
        else GlobalScope.launch { ctx.end() }
    }

    wsHandler.onConnect(connect)
    wsHandler.onMessage(message)
    wsHandler.onError(err)
}

/**
 * Setup a new connection.
 *
 * TODO Deny duplicate names
 *
 * @param context
 * @param name
 * @param exID
 */
private suspend fun newConnection(
    context: WsConnectContext,
    name: Name,
    exID: EC?
) {

    // Get the Existence if it's not null, else get a public one
    val ex = if (exID != null) {
        getExistence(exID) ?: return context.close(Err.EX_NOT_FOUND)
    } else getNextPublicExistence() ?: return context.close(Err.INTERNAL)

    ex.modify {
        val (tkn, qdy) = join(name, context)

        //context.session.idleTimeout = IDLE_TIMOUT_MS

        context.initWith(ex, qdy, tkn)

        broadcast(packetOf(ENTER, qdy), context.sessionId)
    }

    sl.slog("Init session. Ex=${ex.id} SID=${context.sessionId}")
}

/**
 * Attempt a reconnection with a [token]
 *
 * @param context
 * @param token
 */
private fun reconnect(context: WsConnectContext, token: Token) {
    // Attempt to validate the token
    val (info, tkn) = token.refresh() ?: return context.close(Err.BAD_TOKEN)
    // Attempt to locate existence
    val ex = getExistence(info.ec) ?: return context.close(Err.EX_NOT_FOUND)
    val q = ex.qOf(info.qc) ?: return context.close(Err.Q_NOT_FOUND)
    //context.session.idleTimeout = IDLE_TIMOUT_MS
    context.initWith(ex, q, tkn)
    ex.broadcast(packetOf(ENTER, q), context.sessionId)
    sl.slog("Init reconnect session. Ex=${ex.id} SID=${context.sessionId}")
}

private suspend fun handleMessage(context: WsMessageContext) {
    val sd = context.info ?: return
    val packet = context.messageAsClass<Packet>()
    val ex = getExistence(sd.ec)
    val qd = sd.let { ex?.qOf(it.qc) }
    when (packet.type) {
        ACTION -> {
            if (ex != null && qd != null && packet.data != null)
                packet.data!!.asAction
                    ?.let { handleAction(ex, qd, context, it) }
                    ?: context.send(
                        pack(ERR, "no action with name ${packet.data}")
                    )
        }
        CHAT -> {
            if (ex == null || qd == null) sl.elog("Failed to handle chat.")
            else handleChat(ex, qd, packet.data as String)
        }
        PING -> context.send(pack(PING, "pong"))
        else -> context.send("client cannot make ${packet.type} requests.")
    }
}

/** Handle chat broadcasting and parsing. */
private suspend fun handleChat(ex: Existence, qd: Quiddity, text: String) {
    ex.modify {
        // Update chat
        ex.chat.update(qd, text).let { m -> ex.broadcast(chatPacket(m)) }
        // Parse for keywords
        if (qd.ouch.add(text.parseOof)) ex.broadcast(packetOf(QUIDDITY, qd))
    }
}

private fun handleAction(
    ex: Existence,
    qd: Quiddity,
    context: WsMessageContext,
    action: Action
): Unit = TODO()

fun WsConnectContext.close(pair: Pair<Int, String>) =
    session.close(pair.first, pair.second)

val handler = CoroutineExceptionHandler { _, thr ->
    thr.cause?.printStackTrace() ?: thr.printStackTrace()
}
