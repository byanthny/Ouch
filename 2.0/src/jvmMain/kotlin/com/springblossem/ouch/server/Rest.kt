package com.springblossem.ouch.server

import com.springblossem.ouch.common.Auth
import com.springblossem.ouch.common.EndPoint
import com.springblossem.ouch.common.Registration
import com.springblossem.ouch.common.RestErrorResponse.ALREADY_LOGGED_IN
import com.springblossem.ouch.common.RestErrorResponse.DUPLICATE_NAME
import com.springblossem.ouch.common.RestErrorResponse.INVALID_PASSWORD
import com.springblossem.ouch.common.RestErrorResponse.MALFORMED_BODY
import com.springblossem.ouch.common.validatePassword
import com.springblossem.ouch.server.db.get
import com.springblossem.ouch.server.db.new
import io.ktor.http.HttpStatusCode
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
        return@post call.respond(ALREADY_LOGGED_IN.code, ALREADY_LOGGED_IN.message)
      }

      // validate body
      val registration = runCatching { call.receive<Registration>() }
        .getOrNull()
        ?: return@post call.respond(MALFORMED_BODY.code, MALFORMED_BODY.message)

      // check duplicates
      Auth[registration.username]
        ?.let { return@post call.respond(DUPLICATE_NAME.code, DUPLICATE_NAME.message) }

      // validate password
      validatePassword(registration.password)
        .exceptionOrNull()
        ?.let { return@post call.respond(INVALID_PASSWORD.code, it.message!!) }

      registration.password.encrypt()
        .getOrNull()
        ?.let { Auth.new(registration.username, it) }
        ?.let { call.respond(HttpStatusCode.Created, Auth(it, registration.username)) }
        ?: return@post call.respond(HttpStatusCode.InternalServerError)
    }
  }
}
