package com.springblossem.ouch.common

import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient
import kotlin.Result.Companion.failure
import kotlin.Result.Companion.success

@Serializable
data class Auth(
  val id: Int,
  val username: String,
  @Transient val hash: String? = null,
)

@Serializable
data class Registration(val username: String, val password: String)

fun validatePassword(pass: String): Result<Unit> = when {
  pass.length !in 8..32                                              ->
    failure(Error("password must be 8-32 char"))

  !pass.matches("""^(?=.*[A-Za-z])(?=.*\d)[A-Za-z\d]+$""".toRegex()) ->
    failure(Error("""password must match ^(?=.*[A-Za-z])(?=.*d)[A-Za-zd]+$"""))

  else                                                          -> success(Unit)
}

