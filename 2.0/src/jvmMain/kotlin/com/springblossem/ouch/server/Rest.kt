package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.api.EndPoint
import com.springblossem.ouch.common.Registration
import com.springblossem.ouch.common.api.RestErrorResponses.ALREADY_LOGGED_IN
import com.springblossem.ouch.common.api.RestErrorResponses.DUPLICATE_NAME
import com.springblossem.ouch.common.api.RestErrorResponses.MALFORMED_BODY
import com.springblossem.ouch.common.validatePassword
import com.springblossem.ouch.server.db.get
import com.springblossem.ouch.server.db.new
import io.ktor.http.HttpStatusCode
import io.ktor.http.HttpStatusCode.Companion.BadRequest
import io.ktor.http.HttpStatusCode.Companion.Conflict
import io.ktor.http.HttpStatusCode.Companion.Unauthorized
import io.ktor.server.application.Application
import io.ktor.server.application.call
import io.ktor.server.auth.principal
import io.ktor.server.http.content.staticResources
import io.ktor.server.request.receive
import io.ktor.server.response.respond
import io.ktor.server.routing.get
import io.ktor.server.routing.post
import io.ktor.server.routing.routing

fun Application.api() {
  routing {
    staticResources("/static", "assets")
    get(EndPoint.HOME()) { call.respond("STATUS: OK") }
    post(EndPoint.REGISTER()) {
      if (call.principal<AuthPrincipal>() != null) {
        return@post call.respond(Unauthorized, ALREADY_LOGGED_IN)
      }

      // validate body
      val registration = runCatching { call.receive<Registration>() }
        .getOrNull()
        ?: return@post call.respond(BadRequest, MALFORMED_BODY)

      // check duplicates
      Auth[registration.username]
        ?.let { return@post call.respond(Conflict, DUPLICATE_NAME) }

      // validate password
      validatePassword(registration.password)
        .exceptionOrNull()
        ?.let { return@post call.respond(BadRequest, it.message!!) }

      registration.password.encrypt()
        .getOrNull()
        ?.let { Auth.new(registration.username, it) }
        ?.let { call.respond(HttpStatusCode.Created, Auth(it, registration.username)) }
        ?: return@post call.respond(HttpStatusCode.InternalServerError)
    }
  }
}
