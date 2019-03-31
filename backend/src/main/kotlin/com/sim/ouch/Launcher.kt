package com.sim.ouch

import com.sim.ouch.logic.*
import com.sim.ouch.web.Dao
import io.javalin.Javalin
import io.javalin.json.JavalinJson
import io.javalin.websocket.WsSession

const val LOGIN = "/login"
const val SOCKET = "/ws"

val ER_NO_NAME = 4004 to "no name"
val ER_BAD_ID  = 4005 to "unknown ID"

val DAO: Dao by lazy { Dao() }
val javalin: Javalin by lazy { Javalin.create() }


fun main() = javalin.apply {

    ws(SOCKET) { ws ->
        ws.apply {
            onConnect { session ->

                val name: String? = session.queryParam("name")
                if (name.isNullOrBlank())
                    return@onConnect session.close(ER_NO_NAME)

                val exist: Existence = session.queryParam("exID")?.let {
                    DAO.getEx(it) ?: return@onConnect session.close(ER_BAD_ID)
                } ?: DefaultExistence("$name's Existence", -1, Quidity())
                DAO.sessions.getOrPut(exist) { mutableListOf() }.add(session)

                session.send(JavalinJson.toJson(exist))
            }

            onMessage { session, msg ->
                println("Message from: ${session.host()}\n\t$msg")
                val a = JavalinJson.fromJson(msg, OuchAction::class.java)
                println("Actions: $a")
                session.send("{\"name\":\"penis\"}")
            }

            onClose { session, statusCode, reason ->
                when (statusCode) {
                    ER_NO_NAME.first -> { println("no name") }
                    ER_BAD_ID.first -> { println("bad id") }
                    else -> { println("close on $statusCode=$reason") }
                }
            }
        }
    }

    start(System.getenv("PORT")?.toInt() ?: 7000)
}.unit

data class OuchAction(val name: String)

fun WsSession.close(pair: Pair<Int, String>) = close(pair.first, pair.second)
