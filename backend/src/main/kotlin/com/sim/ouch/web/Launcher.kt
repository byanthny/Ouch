package com.sim.ouch.web

import com.sim.ouch.logic.*
import io.javalin.Javalin
import io.javalin.json.JavalinJson

const val LOGIN = "/login"
const val SOCKET = "/ws"

val DAO: Dao by lazy { Dao() }
lateinit var javalin: Javalin


fun main() {

    javalin = Javalin.create().apply {

        ws(SOCKET) {
            var valid: Boolean = false

            it.onConnect { session ->
                val name: String = session.queryParam("name")
                        ?: return@onConnect session.close(4004, "No Name")
                val exist: Existence = session.queryParam("exID")?.let {
                    DAO.existences[it]
                        ?: return@onConnect session.close(4005, "Unknown ID")
                } ?: DefaultExistence("$name's Existence", -1, Quidity())
                DAO.sessions.getOrPut(exist) { mutableListOf() }.add(session)
                session.send(JavalinJson.toJson(exist))
            }

            it.onMessage { session, msg ->
                val action = JavalinJson.fromJson(msg, SocketAction::class.java)
                TODO()
            }
            it.onClose { session, statusCode, reason ->
                TODO()
            }
        }

    }

    javalin.start(8080)
}

sealed class SocketAction(val name: String) {

}
