package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quidity
import io.javalin.websocket.WsSession
import org.eclipse.jetty.websocket.api.Session
import java.util.concurrent.ConcurrentHashMap


val DAO: Dao by lazy { Dao() }

val WsSession.existence get() = DAO.getExistence(id)
val WsSession.quidity get() = DAO.getQuidity(id)

class Dao {
    /** [Existence.id] -> [Existence] */
    private val existences = ConcurrentHashMap<String, Existence>()
    /** [WsSession.id] -> <[Existence], [Quidity]> */
    private val sessions =
            ConcurrentHashMap<String, Pair<Existence, Quidity>>()

    /** contains [Existence.id]. */
    operator fun contains(exID: String) = existences.containsKey(exID)

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

    fun getEx(id: String) = existences[id]

    fun getExistences() = existences.values
    fun getSessions() = sessions.keys().toList()

}
