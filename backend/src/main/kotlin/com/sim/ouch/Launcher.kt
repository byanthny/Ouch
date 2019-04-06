package com.sim.ouch

import com.sim.ouch.EndPoints.*
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.*
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
import java.lang.System.getenv


enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs")
}

val javalin: Javalin by lazy { Javalin.create().prefer405over404() }

class OuchData(val version: String, val uri: String, vararg val authors: String)

val OUCH = OuchData("0.0.0", "https://anthnyd.github.io/Ouch/",
    "Jonathan Augustine", "Anthony Das")

fun main() = javalin.apply {
    enableRouteOverview("/route")
    enableCorsForAllOrigins()
    //defaultContentType("application/json")

    get("/") { it.redirect(OUCH.uri) }
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

    secret(this)
    start(getenv("PORT")?.toInt() ?: 7000.also { javalin.enableDebugLogging() })
}.unit
