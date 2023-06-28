package com.springblossem.ouch.common

import kotlinx.serialization.Serializable

/**
 * The simulation
 *
 * @meta Chat room
 *
 * @property id Unique ID
 * @property name Non-unique name of the [Existence]
 * @property capacity Maximum [Quiddity] capacity of the [Existence]
 * @property public Whether this [Existence] is publicly discoverable
 * @property createdAt Creation timestamp
 */
@Serializable
data class Existence(
  val id: Int,
  val name: String,
  val createdAt: Long,
  val public: Boolean = true,
  val capacity: Int? = null,
  val dormantAt: Long? = null
)

/**
 * Existence chat message
 *
 * @property senderID ID of the [Quiddity] which created this message
 * @property existenceID ID of the [Existence] this message was sent in
 * @property createdAt Creation timestamp
 * @property content Message content
 */
data class ChatMessage(
  val id: Int,
  val senderID: Int,
  val existenceID: Int,
  val createdAt: Long,
  val content: String
)
