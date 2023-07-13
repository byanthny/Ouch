package com.springblossem.ouch.client.api

import com.springblossem.ouch.common.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.engine.js.Js
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.defaultRequest
import io.ktor.client.plugins.logging.DEFAULT
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType.Application
import io.ktor.http.HttpStatusCode
import io.ktor.http.URLProtocol
import io.ktor.http.contentType
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

private val client = HttpClient(Js) {
  install(Logging) {
    logger = Logger.DEFAULT
    level = LogLevel.BODY
  }
  install(ContentNegotiation) {
    json(Json { ignoreUnknownKeys = true })
  }
  defaultRequest {
    url {
      host = "127.0.0.1" // TODO server URL
      port = 7000
      protocol = URLProtocol.HTTP
    }
    contentType(Application.Json)
  }
}

suspend fun register(username: String, password: String): Failable<Auth> {
  val response = client.post(EndPoint.REGISTER()) {
    setBody(Registration(username, password))
  }
  return when (response.status) {
    HttpStatusCode.Created -> Success(response.body())
    else                   -> Failure(response.bodyAsText().takeIf { it.isNotEmpty() })
  }
}

