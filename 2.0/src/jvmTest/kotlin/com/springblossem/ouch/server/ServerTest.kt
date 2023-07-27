package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.Registration
import com.springblossem.ouch.common.RestErrorResponses.DUPLICATE_NAME
import com.springblossem.ouch.server.db.AuthTable
import com.springblossem.ouch.server.db.ExistenceTable
import com.springblossem.ouch.server.db.connectDB
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.basicAuth
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

const val USERNAME = "USERNAME"
const val PASSWORD = "PASSWORD1"
val AUTH = Auth(1, USERNAME)

class ServerTest {

  private lateinit var db: Database

  @BeforeEach
  fun db() {
    db = connectDB(
      user = "server",
      password = "server",
      url = "jdbc:postgresql://localhost:8801/postgres",
    )
  }

  @AfterEach
  fun after() {
    transaction(db) {
      SchemaUtils.drop(ExistenceTable, AuthTable)
    }
  }

  @Test
  fun `registration works`() = testApplication {
    val client = createClient { install(ContentNegotiation) { json() } }
    application { server() }
    client.post(EndPoint.REGISTER()) {
      contentType(Application.Json)
      setBody(Registration(USERNAME, PASSWORD))
    }
      .apply { assertEquals(HttpStatusCode.Created, status) }
      .apply { assertEquals(AUTH, body<Auth>()) }
  }

  @Test
  fun `registration rejects duplicate username`() = testApplication {
    val client = createClient { install(ContentNegotiation) { json() } }
    application { server() }
    `registration works`()
    client.post(EndPoint.REGISTER()) {
      contentType(Application.Json)
      setBody(Registration(USERNAME, PASSWORD))
      basicAuth(USERNAME, PASSWORD)
    }
      .apply { assertEquals(HttpStatusCode.Conflict, status) }
      .apply { assertEquals(DUPLICATE_NAME, call.response.bodyAsText()) }
  }

  @Test
  fun `registration rejects invalid password`() = testApplication {
    val client = createClient { install(ContentNegotiation) { json() } }
    application { server() }
    // too short
    client.post(EndPoint.REGISTER()) {
      contentType(Application.Json)
      setBody(Registration(USERNAME, "wrong"))
      basicAuth(USERNAME, PASSWORD)
    }
      .apply { assertEquals(HttpStatusCode.BadRequest, status) }
    // no numbers
    client.post(EndPoint.REGISTER()) {
      contentType(Application.Json)
      setBody(Registration(USERNAME, "wrongwrong"))
      basicAuth(USERNAME, PASSWORD)
    }
      .apply { assertEquals(HttpStatusCode.BadRequest, status) }
  }

  @Test
  fun `rejects unauthenticated ws connection`() = testApplication {
    // TODO
    //    val client = createClient {
    //      install(ContentNegotiation) { json() }
    //      WebSockets {
    //        contentConverter = KotlinxWebsocketSerializationConverter(Json)
    //        pingInterval = 15.seconds.inWholeMilliseconds
    //        maxFrameSize = Long.MAX_VALUE
    //      }
    //    }
    //    application { server() }
    //    val session = client.webSocketSession(EndPoint.SOCKET())
    //    runCatching {
    //      val frame = session.incoming.receive()
    //      println(frame.frameType)
    //    }
    //    assert(!session.isActive)
    //    assertEquals(UNAUTHENTICATED.description, session.closeReason.await()?.message)
  }

  // TODO Test WS onConnect
  // TODO Test WS onMessage
  // TODO Test WS onError
  // TODO Test WS onExit
}
