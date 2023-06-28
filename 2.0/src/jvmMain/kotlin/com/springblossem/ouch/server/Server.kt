package com.springblossem.ouch.server

import com.springblossem.ouch.common.Existence
import com.springblossem.ouch.server.db.connectDB
import com.springblossem.ouch.server.db.new
import io.ktor.http.HttpStatusCode
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.engine.embeddedServer
import io.ktor.server.html.respondHtml
import io.ktor.server.http.content.resources
import io.ktor.server.http.content.static
import io.ktor.server.netty.Netty
import io.ktor.server.routing.get
import io.ktor.server.routing.routing
import kotlinx.html.*
import java.util.*

fun HTML.index() {
  head {
    title("Hello from Ktor!")
  }
  body {
    div {
      +"Hello from Ktor"
    }
    div {
      id = "root"
    }
    script(src = "/static/untitled.js") {}
  }
}

fun main() {
  connectDB()
  val id = Existence.new("new existence ${Date().time}", Date().time)
  println(id)

  embeddedServer(
    Netty,
    port = 8080,
    host = "127.0.0.1",
    module = Application::server
  ).start(wait = true)
}

fun Application.server() {
  routing {
    get("/") {
      call.respondHtml(HttpStatusCode.OK, HTML::index)
    }
    static("/static") {
      resources()
    }
  }
}
