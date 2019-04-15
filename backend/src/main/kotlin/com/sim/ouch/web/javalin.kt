package com.sim.ouch.web

import com.sim.ouch.OUCH
import com.sim.ouch.logic.*
import com.sim.ouch.secret
import com.sim.ouch.web.EndPoints.*
import io.javalin.*
import kotlinx.coroutines.runBlocking
import kotlinx.html.TagConsumer
import kotlinx.html.div
import kotlinx.html.stream.createHTML

enum class EndPoints(val point: String) {
    ACTIONS("/actions"), SOCKET("/ws"), STATUS("/status"),
    ENDPOINTS("/map"), LOGS("/logs"), ACHIVEMENTS("/achivements"),
    LOGIN("/login"), SIGNUP("/signup"), USER("/user/:id"),
    EXISTENCE_CREATE("/existence")
}

private val port get() = System.getenv("PORT")?.toIntOrNull() ?: 7_000


val server: Javalin by lazy {
    Javalin.create().apply {
        ws(SOCKET.point, Websocket)
        // Auth endpoints
        post(SIGNUP.point) {
            val (name, pass) = it.getCredentials()
            val ud = runBlocking { signup(name, pass) }
            it.json(ud.sendPacket(authTokenOf(ud)))
        }
        post(LOGIN.point) {
            val (name, pass) = it.getCredentials()
            val ud = runBlocking { login(name, pass) }
            it.json(ud.sendPacket(authTokenOf(ud)))
        }
        // API Endpoints
        get(USER.point) {
            val token = it.header("token") ?: throw UnauthorizedResponse()
            val ud = try {
                runBlocking { token.readAuth() } ?: throw NotFoundResponse()
            } catch (e: Exception) {
                throw UnauthorizedResponse("invalid token")
            }
            if (it.pathParam("id") != ud._id) throw UnauthorizedResponse()
            it.json(ud.sendPacket())
        }
        post(EXISTENCE_CREATE.point) {
            TODO("auth, then add to DB then add to user")
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
        exception(Exception::class.java) { e: Exception, ctx: Context ->
            ctx.html { div { text("""Encountered Err: ${e.message}""") } }
        }
        enableCorsForAllOrigins()
        System.getenv("PORT") ?: enableDebugLogging()
        secret(this)
    }.start(port)
}

data class UserPass(val username: String, val password: CharArray)

/**
 * Get username & Password from [Context]
 * @throws BadRequestResponse if no user or password passed.
 */
private fun Context.getCredentials(): UserPass {
    val pass = basicAuthCredentials()?.password
            ?: throw BadRequestResponse("no password")
    val usr = basicAuthCredentials()?.username
            ?: throw BadRequestResponse("no username")
    return UserPass(usr, pass.toCharArray())
}

fun Context.html(html: TagConsumer<String>.() -> Unit) =
    this.html(createHTML().apply(html).finalize())

