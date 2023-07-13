package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.server.db.connectDB
import com.springblossem.ouch.server.db.get
import com.springblossem.ouch.server.websocket.socketConfig
import io.ktor.http.HttpMethod
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.Authentication
import io.ktor.server.auth.Principal
import io.ktor.server.auth.basic
import io.ktor.server.cio.CIO
import io.ktor.server.engine.applicationEngineEnvironment
import io.ktor.server.engine.connector
import io.ktor.server.engine.embeddedServer
import io.ktor.server.plugins.callloging.CallLogging
import io.ktor.server.plugins.contentnegotiation.ContentNegotiation
import io.ktor.server.plugins.cors.routing.CORS
import kotlinx.serialization.json.Json
import org.slf4j.event.Level

val PORT = System.getenv("PORT")?.toInt()
val PRODUCTION = PORT != null

data class AuthPrincipal(val id: Int, val username: String) : Principal {
  constructor(auth: Auth) : this(auth.id, auth.username)
}

fun main() {
  connectDB()
  embeddedServer(CIO, applicationEngineEnvironment {
    developmentMode = !PRODUCTION
    watchPaths = listOf("classes", "resources")
    module { server() }
    connector {
      host = "127.0.0.1"
      port = 7000
    }
  }).start(wait = true)
}

fun Application.server() {
  plugins()
  api()
  socketConfig()
}

fun Application.plugins() {
  install(CORS) {
    allowMethod(HttpMethod.Options)
    allowMethod(HttpMethod.Put)
    allowMethod(HttpMethod.Patch)
    allowMethod(HttpMethod.Delete)
    allowNonSimpleContentTypes = true
    allowCredentials = true
    println(methods)
    allowHeaders { true }
    allowOrigins { true }
    anyHost()
  }
  install(Authentication) {
    // Use basic auth username+pass
    basic {
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
    level = Level.TRACE
  }
}
