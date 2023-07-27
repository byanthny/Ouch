package com.sim.ouch.web

import com.sim.ouch.logic.Quiddity
import kotlinx.serialization.Serializable

/** An easy way to make an outgoing typed [Packet] with a message. */
@PacketDsl
fun chatPacket(message: Chat.Message) = packetOf(PacketType.CHAT, message)

@Serializable
class Chat {

    @Serializable
    data class Message internal constructor(
        val authorID: String,
        val authorName: String,
        val content: String
    )

    private val history = mutableListOf<Message>()

    /** update from client */
    fun update(quiddity: Quiddity, content: String): Message {
        historySizing()
        return Message(quiddity.id, quiddity.name, content)
            .also { history.add(it) }
    }

    private fun historySizing() {
        if (history.size !in 0..MAX_SIZE) history.removeAt(0)
    }

    fun getHistory() = history.toList()

    companion object {
        private const val MAX_SIZE = 1_000
    }
}
