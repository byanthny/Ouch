package com.springblossem.ouch.common

import io.ktor.http.HttpStatusCode
import kotlin.time.Duration.Companion.minutes

enum class EndPoint(val path: String) {
  HOME("/"),
  EXISTENCES("/existences"),
  SOCKET("/ws"),
  REGISTER("/auth/register");

  operator fun invoke() = path
}

enum class RestErrorResponse(val code: HttpStatusCode, val message: String) {
  DUPLICATE_NAME(HttpStatusCode.Conflict, "name already exists"),
  ALREADY_LOGGED_IN(HttpStatusCode.Unauthorized, "already logged in"),
  INVALID_PASSWORD(HttpStatusCode.BadRequest, "invalid password"),
  MALFORMED_BODY(HttpStatusCode.BadRequest, "malformed body");
}

enum class SocketCloseCodes(val code: Short, val description: String) {
  ER_EX_NOT_FOUND(4001, "existence not found"),
  ER_NO_NAME(4002, "no name"),
  ER_DUPLICATE_NAME(4003, "duplicate name"),
  ER_BAD_TOKEN(4004, "invalid token"),
  ER_INTERNAL(4005, "internal err"),
  ER_Q_NOT_FOUND(4006, "quiddity not found"),
  UNAUTHENTICATED(4007, "unauthenticated"),
  UNAUTHORIZED(4008, "unauthorized"),
}

object SocketConfig {

  val IDLE_TIMEOUT = 5.minutes
}
