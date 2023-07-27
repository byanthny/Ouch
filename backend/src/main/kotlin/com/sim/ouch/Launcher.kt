package com.sim.ouch

import com.sim.ouch.extension.secret
import com.sim.ouch.extension.unit
import com.sim.ouch.logic.Achievements
import com.sim.ouch.logic.action.Action
import com.sim.ouch.web.*
import io.javalin.Javalin
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import java.lang.System.getenv

object OuchInfo {
  data class Author(val name: String, val url: String)

  const val version = "0.4.6"
  const val url = "https://anthnyd.github.io/Ouch"
  val jono = Author(
    "Jonathan Augustine",
    "https://jonoaugustine.github.io/portfolio/index.html"
  )
  val anthony = Author("Anthony Das", "https://github.com/anthnyd")

  object Settings {

    const val max_dormant_age = 31
  }
}

object EndPoint {

  const val socket = "/ws"
  const val actions = "/actions"
  const val achievements = "/achievements"
  const val logs = "/logs"
  const val map = "/map"
  const val status = "/status"
}

private val port get() = getenv("PORT")?.toIntOrNull() ?: 7_000

/** Base [Javalin] "builder" */
private val javalin
  get() = Javalin.create {
    //it.enableCorsForAllOrigins()
    //it.enableDevLogging()
  }

val static_endpoints = javalin.apply {
  ws(EndPoint.socket, Websocket)
  get("/public") {
    val limit = it.queryParam("limit")?.toIntOrNull() ?: 10_000
    GlobalScope.launch {
      it.result(
        getPublicExistences()
          .filterValues { ex -> !ex.full }
          .keys.toList()
          .subList(0, limit)
          .json()
      )
    }
  }
  get(EndPoint.actions) { it.result(Action.values.json()) }
  get(EndPoint.achievements) { it.result(Achievements.values.json()) }
  get(EndPoint.map) { it.render("/map.html") }
  get(EndPoint.status) { GlobalScope.launch { it.result(status().json()) } }
  get(EndPoint.logs) { GlobalScope.launch { it.result(logs().json()) } }
  get("/") { it.redirect(OuchInfo.url) }
  secret(this)
}!!

fun main() = static_endpoints.start(port).unit
