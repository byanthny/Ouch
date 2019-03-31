package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import io.javalin.websocket.WsSession
import java.util.concurrent.ConcurrentHashMap

class Dao {
    val existences = ConcurrentHashMap<String, Existence>()
    val sessions = ConcurrentHashMap<Existence, MutableList<WsSession>>()

    /*
        1 ID -> 1 ex
        1 ex -> * ses
        * ses -> 1 ex
     */


    fun getEx(id: String) = existences[id]

}
