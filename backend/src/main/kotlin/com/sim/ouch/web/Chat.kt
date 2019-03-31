package com.sim.ouch.web

import com.sim.ouch.logic.Existence

class Chat(@Transient val existence: Existence) {

    inner class Message internal constructor(
            val authorID: String,
            // val dateTime: OffsetDateTime?, TODO
            val content: String
    )

    private var nextID = 0L
    private val history = mutableListOf<Message>()

    /** (User ->)? Server -> All Users */
    fun `update and distrubute`(authorID: String, content: String): Message {
        historySizing()
        return Message(authorID, content).also {
            history.add(it)
            Packet(Packet.DataType.CHAT, it).pack()
                .also { existence.sessions.forEach { _, wss -> wss.send(it) } }
        }
    }

    private fun historySizing() {
        if (history.size !in 0..MAX_SIZE) history.removeAt(0)
    }

    fun getHistory() = history.toList()

    companion object {
        private const val MAX_SIZE = 1_000
    }

}
