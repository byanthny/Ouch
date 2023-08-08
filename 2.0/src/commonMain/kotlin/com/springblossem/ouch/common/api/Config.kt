package com.springblossem.ouch.common.api

import kotlin.time.Duration.Companion.minutes

enum class EndPoint(private val path: String) {
  HOME("/"),
  EXISTENCES("/existences"),
  SOCKET("/ws"),
  REGISTER("/auth/register");

  operator fun invoke() = path
}

object SocketConfig {

  val IDLE_TIMEOUT = 5.minutes
}
