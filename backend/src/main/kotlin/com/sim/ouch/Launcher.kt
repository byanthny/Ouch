package com.sim.ouch

import com.sim.ouch.EndPoints.*
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.DAO
import com.sim.ouch.web.Websocket
import com.sim.ouch.web.json
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
import java.lang.System.getenv


enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs")
}

val javalin: Javalin by lazy { Javalin.create().prefer405over404() }

class OuchData(val version: String, vararg val authors: String)

val OUCH_VERSION = OuchData("0.0.0", "Jonathan Augustine", "Anthony Das")

fun main() = javalin.apply {

    get("/") { it.redirect("https://anthnyd.github.io/Ouch/") }
    ws(SOCKET.point, Websocket)
    get("/public") {
        val limit = it.queryParam("limit")?.toIntOrNull()
        val json = runBlocking { DAO.getPublicExistences() }
            .let { el -> limit?.let { el.subList(0, limit) } ?: el }
            .map { it._id }
            .json()
        it.result(json)
    }
    get(ACTIONS.point) { it.result(Quiddity.Action.values().json()) }
    get(ENDPOINTS.point) { it.render("/map.html") }
    get(STATUS.point) { it.result(runBlocking { DAO.status() }.json()) }
    get(LOGS.point) { it.result(runBlocking { DAO.getLogs() }.json()) }

    enableDebugLogging()
    secret(this)
    start(getenv("PORT")?.toInt() ?: 7000.also { javalin.enableDebugLogging() })
}.unit
