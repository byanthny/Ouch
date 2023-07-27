package com.springblossem.ouch.common

import kotlinx.serialization.Serializable

@Serializable
data class Packet<T : @Serializable Any>(val type: String, val data: T)

inline fun <reified T : @Serializable Any> packetOf(data: T): Packet<T> =
  Packet(data::class.simpleName!!, data)

