package com.sim.ouch.web

import com.sim.ouch.OUCH
import com.sim.ouch.logic.Achievements
import com.sim.ouch.logic.Action
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.signup
import com.sim.ouch.secret
import com.sim.ouch.web.EndPoints.*
import io.javalin.BadRequestResponse
import io.javalin.Javalin
import kotlinx.coroutines.runBlocking

enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs"), ACHIVEMENTS("/achivements"),
    AUTH("/auth"), AUTH_NEW("/auth/new"), USER("/user/:user")
}

private val port get() = System.getenv("PORT")?.toIntOrNull() ?: 7_000

/** Base [Javalin] "builder" */
private val javalin get() = Javalin.create().apply {
    enableCorsForAllOrigins()
    System.getenv("PORT") ?: enableDebugLogging()
}


val server: Javalin by lazy {
    javalin.apply {

        ws(SOCKET.point, Websocket)
        // Auth endpoints
        post(AUTH_NEW.point) {
            val pass = it.basicAuthCredentials()?.password
                ?: throw BadRequestResponse("no password")
            val usr  = it.basicAuthCredentials()?.username
                ?: throw BadRequestResponse("no username")
            signup(usr, pass.toCharArray())
            TODO("")
        }
        post(AUTH.point) {
            TODO()
        }
        get(USER.point) {
            TODO()
        }
        // Static endpoints
        get("/public") {
            val limit = it.queryParam("limit")?.toIntOrNull()
            val json = runBlocking { getPublicExistences() }
                .let { el -> limit?.let { el.subList(0, limit) } ?: el }
                .filterNot(Existence::full).map(Existence::_id).json()
            it.result(json)
        }
        get(ACTIONS.point) { it.result(Action.values.json()) }
        get(ACHIVEMENTS.point) { it.result(Achievements.values.json()) }
        enableRouteOverview("/route")
        get(ENDPOINTS.point) { it.render("/map.html") }
        get(STATUS.point) { it.result(runBlocking { status() }.json()) }
        get(LOGS.point) { it.result(runBlocking { getLogs() }.json()) }
        get("/") { it.redirect(OUCH.uri) }
        secret(this)
    }.start(port)
}
