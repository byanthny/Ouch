package com.springblossem.ouch.common

import kotlin.time.Duration.Companion.minutes

enum class EndPoint(val path: String) {
  HOME("/"),
  EXISTENCES("/existences"),
  SOCKET("/ws"),
  REGISTER("/auth/register");

  operator fun invoke() = path
}

enum class SocketCloseCodes(val code: Int, val description: String) {
  ER_NO_NAME(4005, "no name"),
  ER_EX_NOT_FOUND(4004, "existence not found"),
  ER_Q_NOT_FOUND(4040, "quiddity not found"),
  ER_BAD_TOKEN(4007, "invalid token"),
  ER_INTERNAL(4010, "internal err"),
  ER_DUPLICATE_NAME(4006, "duplicate name");
}

object SocketConfig {

  val IDLE_TIMEOUT = 5.minutes
}
