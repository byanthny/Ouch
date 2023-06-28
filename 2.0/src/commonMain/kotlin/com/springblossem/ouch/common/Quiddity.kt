package com.springblossem.ouch.common

import kotlinx.serialization.Serializable

/**
 * The essence of an Human.
 *
 * @meta User
 */
@Serializable
data class Quiddity(
  val id: Int,
  val existenceID: Int,
  val authID: Int,
  val name: String,
  val level: Int = 1,
  val exp: Float = 0f,
)

