package com.sim.ouch

import com.sim.ouch.logic.*
import com.sim.ouch.web.Dao
import io.javalin.Javalin
import io.javalin.json.JavalinJson

const val LOGIN = "/login"
const val SOCKET = "/ws"

val DAO: Dao by lazy { Dao() }
lateinit var javalin: Javalin


fun main() {

    javalin = Javalin.create().apply {

        post(LOGIN) {
            println(it)
        }

        ws(SOCKET) { ws -> ws.apply {
            var valid: Boolean = false

            onConnect { session ->
                val name: String = session.queryParam("name")
                        ?: return@onConnect session.close(4004, "No Name")
                val exist: Existence = session.queryParam("exID")?.let {
                    DAO.getEx(it)
                        ?: return@onConnect session.close(4005, "Unknown ID")
                } ?: DefaultExistence("$name's Existence", -1, Quidity())
                DAO.sessions.getOrPut(exist) { mutableListOf() }.add(session)
                session.send(JavalinJson.toJson(exist))
            }

            onMessage { session, msg ->
                val action = JavalinJson.fromJson(msg, SocketAction::class.java)
                println(session)
                TODO()
            }
            onClose { session, statusCode, reason ->
                println(session)
                TODO()
            }
        }}

    }

    javalin.start(8080)
}

sealed class SocketAction(val name: String) {

}
