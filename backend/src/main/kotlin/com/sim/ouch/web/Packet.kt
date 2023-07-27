package com.sim.ouch.web

import com.sim.ouch.Token
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quiddity
import io.javalin.websocket.WsConnectContext
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

inline fun <reified T : @Serializable Any> T.json(): String =
  Json.encodeToString(this)

inline fun <reified T : @Serializable Any> quickLoad(json: String): T =
  Json.decodeFromString(json)

/**
 * A Packet is used to send data in a uniform matter
 * between the Server & clients
 *
 * @property type The PacketType to parse from the data
 * @property data The serialized data
 *
 * @author Jonathan Augustine
 */
@Serializable
data class Packet(val type: PacketType, var data: String? = null)

enum class PacketType {
  INIT, QUIDDITY, EXISTENCE, ENTER, EXIT, ACTION, CHAT, INTERNAL, PING, ERR
}

@DslMarker
annotation class PacketDsl

@PacketDsl
inline fun <reified T : @Serializable Any> packetOf(
  type: PacketType,
  t: T?
): Packet = Packet(type, t?.json())

@PacketDsl
inline fun <reified T : @Serializable Any> pack(
  type: PacketType,
  data: T? = null
): String = packetOf(type, data).packed

/** Returns The [Packet] as a JSON [String]. */
@PacketDsl
val Packet.packed: String
  get() = this.json()

/** Attempts to read the [Packet.data] as type [T] */
@PacketDsl
inline fun <reified T : @Serializable Any> Packet.unpack(): T? =
  data?.let { quickLoad(it) }

/** Loads a [Packet] from this [String] as a JSON. */
@PacketDsl
val String.asPacket: Packet
  get() = quickLoad(this)

@Serializable
private class Init(
  val existence: Existence,
  val quiddity: Quiddity,
  val token: Token
)

fun initPacket(ex: Existence, qd: Quiddity, tkn: Token): Packet =
  Packet(PacketType.INIT, Init(ex, qd, tkn).json())

fun WsConnectContext.initWith(
  existence: Existence,
  quiddity: Quiddity,
  token: String
) = send(initPacket(existence, quiddity, token).packed)

/**
 * Broadcast the [packet] to all connected websocket sessions.
 */
@PacketDsl
fun Existence.broadcast(packet: Packet, vararg excludeSessionIDs: String) =
  sessionTokens
    .mapNotNull { it.wsContext }
    .filterNot { it.sessionId in excludeSessionIDs }
    .forEach { it.send(packet.packed) }
