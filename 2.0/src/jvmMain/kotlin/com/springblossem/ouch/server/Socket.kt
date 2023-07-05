package com.springblossem.ouch.server

import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.SocketConfig
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.routing.routing
import io.ktor.server.websocket.DefaultWebSocketServerSession
import io.ktor.server.websocket.WebSockets
import io.ktor.server.websocket.pingPeriod
import io.ktor.server.websocket.webSocket
import kotlinx.serialization.json.Json
import java.time.Duration

fun Application.socketConfig() {
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(Json)
    pingPeriod = Duration.ofSeconds(15)
    timeoutMillis = SocketConfig.IDLE_TIMEOUT.inWholeMilliseconds
    maxFrameSize = Long.MAX_VALUE
    masking = PORT !== null
  }
  routing { webSocket(EndPoint.SOCKET()) { handler(this) } }
}

private val handler: suspend (DefaultWebSocketServerSession) -> Unit = { session ->
  // onConnect
  onConnect(session)
}

private fun onConnect(session: DefaultWebSocketServerSession): Unit = TODO()
