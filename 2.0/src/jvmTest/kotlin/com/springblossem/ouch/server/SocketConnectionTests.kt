package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.api.EndPoint
import com.springblossem.ouch.server.db.AuthTable
import com.springblossem.ouch.server.db.ExistenceTable
import com.springblossem.ouch.server.db.connectDB
import com.springblossem.ouch.server.db.new
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.basicAuth
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import kotlinx.serialization.json.Json
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.time.Duration.Companion.seconds

class SocketConnectionTests {

  private lateinit var db: Database

  @BeforeEach
  fun dbAndUser() {
    db = connectDB(
      user = "server",
      password = "server",
      url = "jdbc:postgresql://localhost:8801/postgres",
    )
    Auth.new(USERNAME, PASSWORD)
  }

  @AfterEach
  fun after() {
    transaction(db) {
      SchemaUtils.drop(ExistenceTable, AuthTable)
    }
  }

  @Test
  fun `rejects unauthenticated connection`() = testApplication {
    val client = createClient {
      install(ContentNegotiation) { json() }
      WebSockets {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingInterval = 15.seconds.inWholeMilliseconds
        maxFrameSize = Long.MAX_VALUE
      }
    }
    application { server() }
    val result = runCatching { client.webSocketSession(EndPoint.SOCKET()) }
    assert(result.isFailure)
  }

  @Test
  fun `accepts authenticated connection`() = testApplication {
    val client = createClient {
      install(ContentNegotiation) { json() }
      WebSockets {
        contentConverter = KotlinxWebsocketSerializationConverter(Json)
        pingInterval = 15.seconds.inWholeMilliseconds
        maxFrameSize = Long.MAX_VALUE
      }
    }
    application { server() }
    val result = runCatching {
      client.webSocketSession(EndPoint.SOCKET()) {
        basicAuth(USERNAME, PASSWORD)
      }
    }
    assert(result.isSuccess)
  }

  // TODO Test WS onError
  // TODO Test WS onExit
}