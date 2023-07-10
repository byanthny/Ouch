package com.springblossem.ouch.server

import com.springblossem.ouch.common.*
import com.springblossem.ouch.server.db.connectDB
import com.springblossem.ouch.server.db.get
import com.springblossem.ouch.server.db.new
import io.ktor.http.HttpHeaders
import io.ktor.http.HttpMethod
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Created
import io.ktor.http.HttpStatusCode.Companion.InternalServerError
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.ApplicationCall
import io.ktor.server.application.call
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.basic
import io.ktor.server.auth.principal
import io.ktor.server.cio.CIO
import io.ktor.server.engine.embeddedServer
import io.ktor.server.http.content.staticResources
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import io.ktor.server.request.path
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.response.respondRedirect
import io.ktor.server.routing.Route
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing
import io.ktor.util.pipeline.PipelineContext
import kotlinx.serialization.json.Json
import org.slf4j.event.Level
import java.util.*

val PORT = System.getenv("PORT")?.toInt()

data class AuthPrincipal(val id: Int, val username: String) : Principal {
  constructor(auth: Auth) : this(auth.id, auth.username)
}

fun main() {
  connectDB()
  val id = Existence.new(Date().time)
  println(id)

  embeddedServer(
    CIO,
    port = 8080,
    host = "127.0.0.1",
    module = Application::server
  ).start(wait = true)
}

fun Application.server() {
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Delete)
    allowMethod(HttpMethod.Patch)
    allowHeader(HttpHeaders.Authorization)
    anyHost() // @TODO: Don't do this in production if possible. Try to limit it.
  }
  install(Authentication) {
    // Use basic auth username+pass
    basic(name = "auth") {
      realm = "ouch-server"
      validate { (username, password) ->
        Auth[username]
          ?.takeIf { verify(password, it.hash!!).isSuccess }
          // Set principal if verified
          ?.let { AuthPrincipal(it) }
      }
    }
  }
  install(ContentNegotiation) {
    json(Json {
      prettyPrint = PORT == null
      ignoreUnknownKeys = true
    })
  }
  install(CallLogging) {
    level = Level.INFO
    filter { call -> call.request.path().startsWith("/") }
  }

  http()
  socketConfig()
}

fun Application.http() {
  routing {
    staticResources("/static", "assets")
    get(EndPoint.HOME) { call.respondRedirect(ApplicationInfo.site) }
    post(EndPoint.REGISTER()) {
      if (call.principal<AuthPrincipal>() != null) {
        return@post call.respond(BadRequest, "already logged in")
      }

      // validate body
      val registration = runCatching { call.receive<Registration>() }
        .getOrNull()
        ?: return@post call.respond(BadRequest, "malformed body")

      // validate password
      validatePassword(registration.password)
        .exceptionOrNull()
        ?.let { return@post call.respond(BadRequest, it.message!!) }

      registration.password.encrypt()
        .getOrNull()
        ?.let { Auth.new(registration.username, it) }
        ?.let { call.respond(Created, Auth(it, registration.username)) }
        ?: return@post call.respond(InternalServerError)
    }
  }
}

private fun Route.get(
  path: EndPoint,
  body: suspend PipelineContext<Unit, ApplicationCall>.(Unit) -> Unit
) = get(path(), body)
