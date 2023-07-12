package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.SocketConfig
import com.springblossem.ouch.server.db.get
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.webSocket
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.Collections.synchronizedSet

private val connections = synchronizedSet(mutableSetOf<Connection>())

data class Connection(
  val id: Int,
  val session: DefaultWebSocketServerSession
)

val Connection.auth get() = Auth[id]

fun Application.socketConfig() {
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(Json)
    pingPeriod = Duration.ofSeconds(15)
    timeoutMillis = SocketConfig.IDLE_TIMEOUT.inWholeMilliseconds
    maxFrameSize = Long.MAX_VALUE
    masking = PORT !== null
  }
  routing {
    authenticate {
      webSocket(EndPoint.SOCKET()) { handler(this) }
    }
  }
}

private val handler: suspend (DefaultWebSocketServerSession) -> Unit = { session ->
  // onConnect
  onConnect(session)
}

private fun onConnect(session: DefaultWebSocketServerSession): Unit = TODO()
