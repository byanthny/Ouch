package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.JsonConfig
import com.springblossem.ouch.common.api.EndPoint
import com.springblossem.ouch.common.api.UserInfo
import com.springblossem.ouch.common.api.WsMissive
import com.springblossem.ouch.server.db.AuthTable
import com.springblossem.ouch.server.db.ExistenceTable
import com.springblossem.ouch.server.db.connectDB
import com.springblossem.ouch.server.db.new
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.websocket.WebSockets
import io.ktor.client.plugins.websocket.sendSerialized
import io.ktor.client.plugins.websocket.webSocketSession
import io.ktor.client.request.basicAuth
import io.ktor.serialization.kotlinx.KotlinxWebsocketSerializationConverter
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import io.ktor.websocket.Frame
import io.ktor.websocket.readText
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertIs
import kotlin.time.Duration.Companion.seconds

class SocketMessageTests {

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
  fun `userinfo request responds with auth data`() = testApplication {
    application { server() }

    val client = createClient {
      install(ContentNegotiation) { json(JsonConfig) }
      WebSockets {
        contentConverter = KotlinxWebsocketSerializationConverter(JsonConfig)
        pingInterval = 15.seconds.inWholeMilliseconds
        maxFrameSize = Long.MAX_VALUE
      }
    }
    val session =
      client.webSocketSession(EndPoint.SOCKET()) { basicAuth(USERNAME, PASSWORD) }
    session.sendSerialized<WsMissive>(UserInfo.RequestOwn)
    val frame = session.incoming.receive()
    assertIs<Frame.Text>(frame)
    val decoded =
      runCatching { JsonConfig.decodeFromString<WsMissive>(frame.readText()) }
    assert(decoded.isSuccess) { decoded.exceptionOrNull()?.message ?: "" }
    val response = decoded.getOrThrow()
    assertIs<UserInfo.Response>(response)
    assertEquals(1, response.auth.id)
    assertEquals(USERNAME, response.auth.username)
  }
}
