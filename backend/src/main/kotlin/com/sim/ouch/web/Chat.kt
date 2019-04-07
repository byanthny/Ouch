package com.sim.ouch.web

import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.Packet.DataType.CHAT

/** An easy way to make an outgoing typed [Packet] with a message. */
fun chatPacket(message: Chat.Message) = Packet(CHAT, message)

class Chat {

    data class Message internal constructor(
        val authorID: String,
        val authorName: String,
        val content: String
    )

    private var nextID = 0L
    private val history = mutableListOf<Message>()

    /** update from client */
    fun update(quiddity: Quiddity, content: String): Message {
        historySizing()
        return Message(quiddity.id, quiddity.name, content).also { history.add(it) }
    }

    private fun historySizing() {
        if (history.size !in 0..MAX_SIZE) history.removeAt(0)
    }

    fun getHistory() = history.toList()

    companion object {
        private const val MAX_SIZE = 1_000
    }
}
