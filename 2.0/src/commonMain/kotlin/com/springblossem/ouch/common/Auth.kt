package com.springblossem.ouch.common

data class Auth(
  val id: Int,
  val username: String,
  val hash: String,
)
