package com.sim.ouch

import com.sim.ouch.EndPoints.*
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.*
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
import kotlinx.html.*
import kotlinx.html.stream.createHTML
import java.lang.System.getenv

val test = true

enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs")
}

val javalin: Javalin by lazy { Javalin.create().prefer405over404() }

class OuchData(val version: String, vararg val authors: String)

val OUCH_VERSION = OuchData("0.0.0", "Jonathan Augustine", "Anthony Das")

fun main() = javalin.apply {

    get("/") { it.redirect("https://anthnyd.github.io/Ouch/") }
    get(ACTIONS.point) { it.result(Quiddity.Action.values().json()) }
    get(ENDPOINTS.point) { it.render("/map.html") }
    ws(SOCKET.point, Websocket)
    get(STATUS.point) { it.result(runBlocking { DAO.status() }.json()) }
    get(LOGS.point) {
        it.result(runBlocking { DAO.getLogs() }.json())
    }

    secret(this)
    start(getenv("PORT")?.toInt() ?: 7000.also { javalin.enableDebugLogging() })
}.unit
