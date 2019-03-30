package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import io.javalin.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap

class Dao {
    val existences = ConcurrentHashMap<String, Existence>()
    val sessions = ConcurrentHashMap<Existence, List<WsSession>>()
    fun getEx(id: String) = existences[id]
    fun getSessions(existence: Existence) = sessions[existence]
    fun getSession(existence: Existence, index: Int) =
        getSessions(existence)?.get(index)
}
