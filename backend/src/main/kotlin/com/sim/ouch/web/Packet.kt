package com.sim.ouch.web

import com.google.gson.Gson
import com.google.gson.GsonBuilder

val gson: Gson by lazy { GsonBuilder().create() }

fun Any.json(): String = gson.toJson(this)

inline fun <reified T> quickLoad(json: String) =
        gson.fromJson(json, T::class.java)!!

/** Loads a Packet from a JSON */
fun readPacket(json: String) = quickLoad<Packet>(json)
//
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
        var data: Any,
        @Transient val prebuild: Boolean = false
) {

    enum class DataType { QUIDITY, EXISTENCE, ACTION, CHAT, INTERNAL }

    init {
        data = if (prebuild) data else gson.toJson(data)!!
    }

    /** Returns The [Packet] as a JSON [String]. */
    fun pack() = gson.toJson(this)!!

    /** Returns The [data] unpacked from a JSON string. */
    inline fun <reified T> unpack() = quickLoad<T>(data as String)

    override fun toString() = "$dataType:$data"
}
