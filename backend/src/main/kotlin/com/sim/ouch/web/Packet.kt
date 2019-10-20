package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.Packet.DataType.INIT
import io.javalin.websocket.WsConnectContext
import io.javalin.websocket.WsContext
import kotlinx.serialization.*
import kotlinx.serialization.json.Json

@UnstableDefault
@ImplicitReflectionSerializer
inline fun <reified T : @Serializable Any> T.json(): String =
    Json.nonstrict.stringify(T::class.serializer(), this)

@UnstableDefault
@ImplicitReflectionSerializer
inline fun <reified T : @Serializable Any> quickLoad(json: String): T =
    Json.nonstrict.parse(json)

/** Loads a Packet from a JSON */
@UnstableDefault
@ImplicitReflectionSerializer
fun readPacket(json: String) = quickLoad<Packet>(json)

@DslMarker
annotation class PacketDsl

@ImplicitReflectionSerializer
@PacketDsl
fun pack(type: Packet.DataType, data: Any? = null) = Packet(type, data).pack()

/**
 * A Packet is used to send data in a uniform matter
 * between the Server & clients
 *
 * @property dataType The DataType to parse from the data
 * @property data The Object being sent
 * @property prebuild Whether the data is already in JSON
 *
 * @author Jonathan Augustine
 */
@ImplicitReflectionSerializer
data class Packet(
    val dataType: DataType,
    var data: @Serializable Any? = null,
    @Transient val prebuild: Boolean = false
) {

    enum class DataType {
        INIT, QUIDDITY, EXISTENCE, ENTER, EXIT, ACTION, CHAT, INTERNAL, PING
    }

    init {
        data = if (prebuild) data else data?.json()
    }

    /** Returns The [Packet] as a JSON [String]. */
    @ImplicitReflectionSerializer
    fun pack() = this.json()

    /** Returns The [data] unpacked from a JSON string. */
    @ImplicitReflectionSerializer
    inline fun <reified T : @Serializable Any> unpack() =
        data?.let { quickLoad<T>(data as String) }

    override fun toString() = "$dataType:$data"
}

data class InitPacket(
    val existence: Existence,
    val quiddity: Quiddity,
    val token: String
)

@ImplicitReflectionSerializer
fun WsConnectContext.initWith(existence: Existence, quiddity: Quiddity, token: String) =
    send(Packet(INIT, InitPacket(existence, quiddity, token)).pack())

/** Broadcast the [packet] to all connected websocket sessions. */
@ImplicitReflectionSerializer
fun Existence.broadcast(packet: Packet) =
    sessionTokens.forEach { it.wsContext?.send(packet.pack()) }

@ImplicitReflectionSerializer
fun Existence.broadcast(
    dataType: Packet.DataType,
    data: Any,
    isString: Boolean = false,
    vararg excludeIDs: String
) = sessionTokens.mapNotNull { it.wsContext }
    .broadcast(dataType, data, isString, *excludeIDs)

@ImplicitReflectionSerializer
private fun Iterable<WsContext>.broadcast(
    dataType: Packet.DataType,
    data: Any,
    isString: Boolean = false,
    vararg excludeIDs: String
) = filterNot { it.sessionId in excludeIDs }
    .forEach { it.send(Packet(dataType, data, isString).pack()) }
