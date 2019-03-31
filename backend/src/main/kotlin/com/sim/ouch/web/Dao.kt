package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quidity
import io.javalin.websocket.WsSession
import org.eclipse.jetty.websocket.api.Session
import java.util.concurrent.ConcurrentHashMap

class Dao {
    private val existences  = ConcurrentHashMap<String, Existence>()
    private val sessions    = ConcurrentHashMap<String, Existence>()
    private val sessionQuid = ConcurrentHashMap<String, Quidity>()

    /*
        1 ID -> 1 ex | 1 ex -> * ses
        * ses -> 1 ex
     */


    fun getEx(id: String) = existences[id]

}
