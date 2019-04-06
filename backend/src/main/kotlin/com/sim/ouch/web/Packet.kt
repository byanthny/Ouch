package com.sim.ouch.web

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.Packet.DataType.INIT
import io.javalin.websocket.WsSession

val gson: Gson by lazy { GsonBuilder().setPrettyPrinting().create() }

fun Any.json(): String = gson.toJson(this)

inline fun <reified T> quickLoad(json: String) =
        gson.fromJson(json, T::class.java)!!

/** Loads a Packet from a JSON */
fun readPacket(json: String) = quickLoad<Packet>(json)

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
data class Packet(
        val dataType: DataType,
        var data: Any? = null,
        @Transient val prebuild: Boolean = false
) {

    enum class DataType {
        INIT, QUIDITY, EXISTENCE, ENTER, EXIT, ACTION, CHAT, INTERNAL, PING
    }

    init {
        data = if (prebuild) data else gson.toJson(data)!!
    }

    /** Returns The [Packet] as a JSON [String]. */
    fun pack() = gson.toJson(this)!!

    /** Returns The [data] unpacked from a JSON string. */
    inline fun <reified T> unpack() = data?.let { quickLoad<T>(data as String) }

    override fun toString() = "$dataType:$data"
}

data class InitPacket(
    val existence: Existence,
    val quiddity: Quiddity,
    val token: String
)

fun WsSession.initWith(existence: Existence, quiddity: Quiddity, token: String) {
    send(Packet(INIT, InitPacket(existence, quiddity, token)).pack())
}

/** Broadcast the [packet] to all connected sessions. */
fun Existence.broadcast(packet: Packet) {
    val s = packet.pack()
    sessionTokens.forEach { DAO.getSession(it)?.send(s) }
}

fun Existence.broadcast(
    dataType: Packet.DataType,
    data: Any,
    isString: Boolean = false,
    vararg excludeIDs: String
) = sessionTokens.mapNotNull { DAO.getSession(it) }
    .broadcast(dataType, data, isString, *excludeIDs)

fun Iterable<WsSession>.broadcast(
    dataType: Packet.DataType,
    data: Any,
    isString: Boolean = false,
    vararg excludeIDs: String
) = filterNot { it.id in excludeIDs }
    .forEach { it.send(Packet(dataType, data, isString).pack()) }

