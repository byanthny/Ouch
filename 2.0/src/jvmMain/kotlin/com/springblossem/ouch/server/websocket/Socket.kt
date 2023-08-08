package com.springblossem.ouch.server.websocket

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.JsonConfig
import com.springblossem.ouch.common.api.*
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
import io.ktor.websocket.*
import io.ktor.websocket.Frame.Close
import io.ktor.websocket.Frame.Text
import kotlinx.coroutines.channels.consumeEach
import java.time.Duration
import java.util.Collections.synchronizedMap

private typealias DWSSSession = DefaultWebSocketServerSession

private val connections = synchronizedMap(mutableMapOf<Int, Connection>())
private val sessions = synchronizedMap(mutableMapOf<DWSSSession, Connection>())

private data class Connection(val id: Int, val session: DWSSSession)

private val Connection.auth get() = Auth[id]

fun Application.socketConfig() {
  install(WebSockets) {
    contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
    pingPeriod = Duration.ofSeconds(15)
    timeoutMillis = SocketConfig.IDLE_TIMEOUT.inWholeMilliseconds
    maxFrameSize = Long.MAX_VALUE
    masking = PRODUCTION
  }
  routing {
    authenticate(/*optional = true*/) {
      webSocket(EndPoint.SOCKET()) { handler(this) }
    }
  }
}

private suspend fun handler(session: DWSSSession) {
  // onConnect
  onConnect(session)
  // onMessage
  val error = runCatching { session.readFrames() }
  // TODO onClose/onError
  println(error)
}

private suspend fun onConnect(session: DWSSSession) {
  // authenticate
  val (uid, username) = session.call.principal<AuthPrincipal>()
    ?: return session.close(SocketCloseCodes.UNAUTHENTICATED)
  // save connection info
  Connection(uid, session)
    .also { connections[uid] = it }
    .also { sessions[session] = it }
}

private suspend fun DWSSSession.readFrames() = incoming.consumeEach { frame ->
  when (frame) {
    is Text  -> handleTextFrame(frame.readText())
    is Close -> frame.readReason()
    else     -> {}
  }
}

private suspend fun DWSSSession.handleTextFrame(text: String) {
  val decoded = runCatching { JsonConfig.decodeFromString<WsMissive>(text) }
  if (decoded.isFailure) {
    TODO("handle decode failure")
  }
  when (decoded.getOrThrow()) {
    is ExistenceInfo.Request    -> TODO()
    is ExistenceInfo.RequestOwn -> TODO()
    is UserInfo.RequestOwn      -> sessions[this]?.auth
      ?.let { sendSerialized<WsMissive>(UserInfo.Response(it)) }
    else                        -> Unit
  }
}

private suspend fun DWSSSession.close(closeCode: SocketCloseCodes) =
  close(CloseReason(closeCode.code, closeCode.description))

private val DWSSSession.connection get() = sessions[this]
