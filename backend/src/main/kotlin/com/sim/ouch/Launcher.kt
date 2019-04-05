package com.sim.ouch

import com.sim.ouch.EndPoints.*
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.*
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
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
    ws(SOCKET.point, Websocket)
    get("/public") {
        var limit = it.queryParam("limit")?.toIntOrNull() ?: 100
        if (limit < 1) limit = 100
        launch {
            it.result(
                DAO.getPublicExistences().subList(0, limit)
                .map { it._id }.json()
            )
        }
    }
    get(ACTIONS.point) { it.result(Quiddity.Action.values().json()) }
    get(ENDPOINTS.point) { it.render("/map.html") }
    get(STATUS.point) { it.result(runBlocking { DAO.status() }.json()) }
    get(LOGS.point) { it.result(runBlocking { DAO.getLogs() }.json()) }

    secret(this)
    start(getenv("PORT")?.toInt() ?: 7000.also { javalin.enableDebugLogging() })
}.unit
