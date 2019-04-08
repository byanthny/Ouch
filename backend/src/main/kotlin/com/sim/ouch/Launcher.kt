package com.sim.ouch

import com.sim.ouch.EndPoints.*
import com.sim.ouch.logic.Achievements
import com.sim.ouch.logic.Action
import com.sim.ouch.logic.Existence
import com.sim.ouch.web.DAO
import com.sim.ouch.web.Websocket
import com.sim.ouch.web.json
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking
import java.io.File
import java.lang.System.getenv
import javax.sound.sampled.AudioSystem

enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs"), ACHIVEMENTS("/achivements")
}

class OuchData(val version: String, val uri: String, vararg val authors: String)

val OUCH = OuchData("0.0.0", "https://anthnyd.github.io/Ouch/",
    "Jonathan Augustine", "Anthony Das")

private val port get() = getenv("PORT")?.toIntOrNull() ?: 7_000

/** Base [Javalin] "builder" */
private val javalin get() = Javalin.create().apply {

    enableCorsForAllOrigins()
    getenv("PORT") ?: enableDebugLogging()
}

// val socket_service: Javalin by lazy { javalin.apply { } }

val static_endpoints: Javalin by lazy {
    javalin.apply {
        ws(SOCKET.point, Websocket)
        get("/public") {
            val limit = it.queryParam("limit")?.toIntOrNull()
            val json = runBlocking { DAO.getPublicExistences() }
                .let { el -> limit?.let { el.subList(0, limit) } ?: el }
                .filterNot(Existence::full).map(Existence::_id).json()
            it.result(json)
        }
        get(ACTIONS.point) { it.result(Action.callForms.json()) }
        get(ACHIVEMENTS.point) { it.result(Achievements.values.json()) }
        enableRouteOverview("/route")
        get(ENDPOINTS.point) { it.render("/map.html") }
        get(STATUS.point) { it.result(runBlocking { DAO.status() }.json()) }
        get(LOGS.point) { it.result(runBlocking { DAO.getLogs() }.json()) }
        get("/") { it.redirect(OUCH.uri) }
        get("/music") {
            it.result(AudioSystem.getAudioInputStream(File("backend/red/Halpe - Ocean.mp3")))
        }
        secret(this)
    }
}

fun main() {
    static_endpoints.start(port)
}
