package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.Registration
import com.springblossem.ouch.server.db.AuthTable
import com.springblossem.ouch.server.db.ExistenceTable
import com.springblossem.ouch.server.db.connectDB
import io.ktor.client.call.body
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import io.ktor.server.testing.testApplication
import org.jetbrains.exposed.sql.Database
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction
import org.junit.Before
import org.junit.jupiter.api.AfterEach
import org.junit.jupiter.api.BeforeEach
import kotlin.test.Test
import kotlin.test.assertEquals

const val USERNAME = "USERNAME"
const val PASSWORD = "PASSWORD1"
val AUTH = Auth(1, USERNAME)

class ServerKtTest {

  private lateinit var db: Database

  @BeforeEach
  fun db() {
    db = connectDB()
  }

  @AfterEach
  fun after() {
    transaction(db) {
      SchemaUtils.drop(ExistenceTable, AuthTable)
    }
  }

  @Test
  fun registration() = testApplication {
    val client = createClient { install(ContentNegotiation) { json() } }
    application { server() }
    client.post(EndPoint.REGISTER()) {
      contentType(Application.Json)
      setBody(Registration(USERNAME, PASSWORD))
    }
      .apply { assertEquals(HttpStatusCode.Created, status) }
      .apply { assertEquals(AUTH, body<Auth>()) }
  }
}
