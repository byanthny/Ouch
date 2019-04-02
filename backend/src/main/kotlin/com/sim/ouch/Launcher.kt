package com.sim.ouch

import com.sim.ouch.logic.Quidity
import com.sim.ouch.logic.sessions
import com.sim.ouch.web.*
import io.javalin.Javalin
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.lang.System.getenv


val ER_NO_NAME  = 4004 to "no name"
val ER_BAD_ID   = 4005 to "unknown ID"
val ER_INTERNAL = 4010 to "internal err"

val javalin: Javalin by lazy { Javalin.create().prefer405over404() }

class OuchData(val version: String, vararg val authors: String)

val OUCH_VERSION = OuchData("0.0.0", "Jonathan Augustine", "Anthony Das")

fun main() = javalin.apply {

    ws(EndPoints.SOCKET.point, Websocket)

    get(EndPoints.STATUS.point) {
        val dex = DAO.dexList
        val ex = DAO.exList
        val ss = DAO.numSessions
        val html = createHTML().apply {
            body {
                table {
                    tr {
                        th { text("Live Existences: ${ex.size}") }
                        th { text("Dormant Existences: ${dex.size}") }
                        th { text("Total Sessions: ${ss}") }
                    }
                    tr {
                        th { text("Existence") }
                        th { text("Quidity count") }
                        th { text("Session Count") }
                    }
                    ex.forEach {
                        tr {
                            td { text(it._id) }
                            td { text(it.quidities.size) }
                            td { text(it.sessions?.size ?: 0) }
                        }
                    }
                }
            }
        }.finalize()
        it.html(html)
    }
    get("/") { it.redirect("https://anthnyd.github.io/Ouch/") }
    get(EndPoints.ACTIONS.point) { it.result(Quidity.Action.values().json()) }
    get(EndPoints.ENDPOINTS.point) { it.render("/map.html") }

    secret(this)

    start(getenv("PORT")?.toInt() ?: 7000.also { javalin.enableDebugLogging() })
}.unit
