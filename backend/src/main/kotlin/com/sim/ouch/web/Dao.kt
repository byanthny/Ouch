package com.sim.ouch.web

import com.sim.ouch.LruKache
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quidity
import io.javalin.websocket.WsSession
import kotlinx.coroutines.*
import java.util.concurrent.ConcurrentHashMap


val DAO: Dao by lazy { Dao() }

val WsSession.existence get() = DAO.getExistence(id)
val WsSession.quidity get() = DAO.getQuidity(id)

class Dao {
    /** [Existence.id] -> [Existence] */
    private val existences = ConcurrentHashMap<String, Existence>()
    private val dormantExistences = LruKache<String, Existence>()
    /** [WsSession.id] -> <[Existence], [Quidity]> */
    private val sessions = ConcurrentHashMap<String, Pair<Existence, Quidity>>()

    /** contains [Existence.id]. */
    operator fun contains(exID: String) =
        existences.containsKey(exID) || dormantExistences.containsKey(exID)

    /**
     * Adds a new [WsSession] to an existing [Existence] and returns a new
     * [Quidity] associated with this session.
     */
    fun addSession(session: WsSession, existence: Existence, name: String) :
            Quidity {
        existence.sessions[session.id] = session
        return existence.generateQuidity(name).also {
            existence.enter(it)
            sessions[session.id] = existence to it
        }
    }

    fun newExistence(session: WsSession, existence: Existence): Existence {
        existences[existence.id] = existence.also {
            it.sessions[session.id] = session
            sessions[session.id] = existence to it.initialQuidity
        }
        return existence
    }

    fun getExistence(sessionID: String) = sessions[sessionID]?.first

    fun getQuidity(sessionID: String) = sessions[sessionID]?.second

    /**
     * Returns the [Existence] matching the [id]. If the [Existence] is dormant,
     * it is moved to the active map.
     */
    fun getEx(id: String) = existences[id] ?: dormantExistences[id]?.also {
        dormantExistences.remove(id)!!.let { existences[id] = it }
    }

    fun removeSession(session: WsSession) {
        val pair = sessions[session.id]
        pair?.first?.sessions?.remove(session.id)
        pair?.second // TODO Remove Quidity on exit?

        session.existence?.also { e ->
            if (e.sessions.isEmpty()) {
                dormantExistences[e.id] = existences.remove(e.id)!!
            }
        }
        sessions.remove(session.id)
    }

    fun getExistences() = existences.map { it.value }
    fun getDormantEx() = dormantExistences.map { it.value }
    fun getSessions() = sessions.map { it.key }

    fun statusPacket() = Packet(Packet.DataType.INTERNAL, StatusPacket(
        getExistences() + dormantExistences.map { it.value }, sessions.size
    ))

    val cleaner = GlobalScope.launch {
        while (true) {
            delay(1_000 * 60)
            val exs = existences.filterValues { it.sessions.isEmpty() }.values
            exs.forEach {
                dormantExistences[it.id] = existences.remove(it.id)!!
            }
        }
    }

}

data class StatusPacket(val ex: List<Existence>, val ses: Int)
