package com.sim.ouch

import com.sim.ouch.logic.*
import com.sim.ouch.web.*
import com.sim.ouch.web.Packet.DataType.*
import io.javalin.*
import io.javalin.websocket.WsSession


val ER_NO_NAME = 4004 to "no name"
val ER_BAD_ID  = 4005 to "unknown ID"

val javalin: Javalin by lazy { Javalin.create() }


fun main() = javalin.apply {

    get("/") { it.redirect("https://anthnyd.github.io/Ouch/") }

    get(EndPoints.STATUS.point) { it.result(DAO.statusPacket().pack()) }

    get(EndPoints.ACTIONS.point) {
        it.result(Quidity.Action.values().json())
    }

    get(EndPoints.ENDPOINTS.point) {
        it.html(ENDPONT_MAP)
    }

    ws(EndPoints.SOCKET.point) { ws ->

        ws.onConnect { session ->
            // Attempt to parse name
            val name: String? = session.queryParam("name")
            if (name.isNullOrBlank()) return@onConnect session.close(ER_NO_NAME)
            lateinit var quidity: Quidity
            // Attempt to get an existence
            val exist: Existence = session.queryParam("exID")?.let { id ->
                // With an invalid ID, close
                DAO.getEx(id)?.also { quidity = DAO.addSession(session, it, name) }
                        ?: return@onConnect session.close(ER_BAD_ID)
            } ?: let {
                // with no ID, make new Existence
                DAO.newExistence(session, DefaultExistence(Quidity(name))).also {
                    quidity = it.initialQuidity
                }
            }

            session.send(Packet(INIT, InitPacket(exist, quidity)).pack())
        }

        ws.onMessage { session, msg ->
            val packet = readPacket(msg)
            val ex = DAO.getExistence(session.id)
            when (packet.dataType) {
                QUIDITY -> TODO()
                EXISTENCE -> TODO()
                ACTION -> TODO()
                CHAT -> ex?.chat?.`update and distrubute`(
                    session.quidity!!, packet.data as String
                )
            }
        }

        ws.onClose { _, statusCode, reason ->
            when (statusCode) {
                ER_NO_NAME.first -> println("no name")
                ER_BAD_ID.first -> println("bad id")
                else -> println("close on $statusCode=$reason")
            }
        }
    }

    start(System.getenv("PORT")?.toInt() ?: 7000)
}.unit

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)
