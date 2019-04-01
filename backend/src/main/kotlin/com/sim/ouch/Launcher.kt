package com.sim.ouch

import com.sim.ouch.logic.DefaultExistence
import com.sim.ouch.logic.Quidity
import com.sim.ouch.web.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.Javalin
import io.javalin.UnauthorizedResponse
import io.javalin.websocket.WsSession
import kotlinx.html.*
import kotlinx.html.stream.createHTML


val ER_NO_NAME = 4004 to "no name"
val ER_BAD_ID  = 4005 to "unknown ID"

val javalin: Javalin by lazy { Javalin.create() }


fun main() = javalin.apply {

    ws(EndPoints.SOCKET.point) { ws ->

        ws.onConnect { session ->

            // Attempt to parse name
            val name: String? = session.queryParam("name")
            if (name.isNullOrBlank()) return@onConnect session.close(ER_NO_NAME)
            lateinit var quidity: Quidity

            // Attempt to get an existence
            val exist = session.queryParam("exID")?.let { id ->
                // With an invalid ID, close
                DAO.getEx(id)?.also {
                    quidity = DAO.addSession(session, it, name)
                    // Broadcast the join event to all sessions but this one
                    it.sessions.values.broadcast(ENTER, quidity, session.id)
                } ?: return@onConnect session.close(ER_BAD_ID)
            } ?: let {
                // with no ID, make new Existence
                DAO.newExistence(session, DefaultExistence(Quidity(name)))
                    .also { quidity = it.initialQuidity }
            }

            session.send(Packet(INIT, InitPacket(exist, quidity)).pack())
            session.idleTimeout // TODO pinging
        }

        ws.onMessage { session, msg ->
            val packet = readPacket(msg)
            val ex = DAO.getExistence(session.id)
            when (packet.dataType) {
                QUIDITY -> TODO()
                EXISTENCE -> TODO()
                ACTION -> TODO()
                CHAT -> ex?.chat?.update(
                    session.quidity!!, packet.data as String
                )?.let { ex.sessions.values.broadcast(CHAT, it) }
                else -> throw UnauthorizedResponse(
                    "Client cannot make ${packet.dataType} requests."
                )
            }
        }

        ws.onClose { session, statusCode, reason ->
            when (statusCode) {
                ER_NO_NAME.first -> println("no name")
                ER_BAD_ID.first -> println("bad id")
                else -> println("close on $statusCode=$reason")
            }
            session.quidity?.also {
                session.existence?.sessions?.values?.broadcast(EXIT, it, session.id)
            }
            DAO.removeSession(session)
        }
    }

    get(EndPoints.STATUS.point) {
        val dex = DAO.getDormantEx()
        val ex = DAO.getExistences()
        val ss = DAO.getSessions()
        val html = createHTML().apply {
            body {
                table {
                    tr {
                        th { text("Live Existences: ${ex.size}") }
                        th { text("Dormant Existences: ${dex.size}") }
                        th { text("Total Sessions: ${ss.size}") }
                    }
                    tr {
                        th { text("Existence") }
                        th { text("Quidity count") }
                        th { text("Session Count") }
                    }
                    ex.forEach {
                        tr {
                            td { text(it.id) }
                            td { text(it.quidities.size) }
                            td { text(it.sessions.size) }
                        }
                    }
                }
            }
        }.finalize()
        it.html(html)
    }
    get("/") { it.redirect("https://anthnyd.github.io/Ouch/") }
    get(EndPoints.ACTIONS.point) { it.result(Quidity.Action.values().json()) }
    get(EndPoints.ENDPOINTS.point) { it.html(ENDPONT_MAP) }

    secret(this)

    start(System.getenv("PORT")?.toInt() ?: 7000)
}.unit

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)
