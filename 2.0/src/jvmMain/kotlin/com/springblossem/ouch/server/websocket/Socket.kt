package com.springblossem.ouch.server.websocket

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.SocketCloseCodes
import com.springblossem.ouch.common.SocketConfig
import com.springblossem.ouch.server.AuthPrincipal
import com.springblossem.ouch.server.PRODUCTION
import com.springblossem.ouch.server.db.get
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.server.application.Application
import io.ktor.server.application.install
import io.ktor.server.auth.authenticate
import io.ktor.server.auth.principal
import io.ktor.server.routing.routing
import io.ktor.server.websocket.*
import io.ktor.websocket.CloseReason
import io.ktor.websocket.FrameType.CLOSE
import io.ktor.websocket.FrameType.TEXT
import io.ktor.websocket.close
import kotlinx.coroutines.channels.consumeEach
import kotlinx.serialization.json.Json
import java.time.Duration
import java.util.Collections.synchronizedSet

private typealias DWSSSession = DefaultWebSocketServerSession

private val connections = synchronizedSet(mutableSetOf<Connection>())

data class Connection(
  val id: Int,
  val session: DWSSSession
)

val Connection.auth get() = Auth[id]

fun Application.socketConfig() {
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(Json)
    pingPeriod = Duration.ofSeconds(15)
    timeoutMillis = SocketConfig.IDLE_TIMEOUT.inWholeMilliseconds
    maxFrameSize = Long.MAX_VALUE
    masking = PRODUCTION
  }
  routing {
    authenticate(optional = true) {
      webSocket(EndPoint.SOCKET()) { handler(this) }
    }
  }
}

private suspend fun handler(session: DWSSSession) {
  // onConnect
  onConnect(session)
}

private suspend fun onConnect(session: DWSSSession) {
  // authenticate
  val (uid, username) = session.call.principal<AuthPrincipal>()
    ?: return session.close(SocketCloseCodes.UNAUTHENTICATED)
  TODO("register connection")
}

private suspend fun DWSSSession.readFrames() = incoming.consumeEach { frame ->
  when (frame.frameType) {
    TEXT  -> TODO("handle text frame")
    CLOSE -> TODO("handle close frame")
    else  -> {}
  }
}

private suspend fun DWSSSession.close(closeCode: SocketCloseCodes) =
  close(CloseReason(closeCode.code, closeCode.description))
