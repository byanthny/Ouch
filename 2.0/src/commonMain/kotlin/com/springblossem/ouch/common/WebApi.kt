package com.springblossem.ouch.common

import kotlin.time.Duration.Companion.minutes

enum class EndPoint(private val path: String) {
  HOME("/"),
  EXISTENCES("/existences"),
  SOCKET("/ws"),
  REGISTER("/auth/register");

  operator fun invoke() = path
}

object RestErrorResponses {

  const val DUPLICATE_NAME = "name already exists"
  const val ALREADY_LOGGED_IN = "already logged in"
  const val INVALID_PASSWORD = "invalid password"
  const val MALFORMED_BODY = "malformed body"
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

