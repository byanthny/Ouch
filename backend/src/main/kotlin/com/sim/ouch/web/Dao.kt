package com.sim.ouch.web

import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Existence.Status.DORMANT
import com.sim.ouch.logic.Existence.Status.DRY
import com.sim.ouch.logic.Quidity
import io.javalin.websocket.WsSession
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.util.concurrent.ConcurrentHashMap
import kotlin.collections.set


val DAO: Dao by lazy { Dao() }
private val mongo by lazy {
    KMongoConfiguration.extendedJsonMapper.enableDefaultTyping()
    KMongo.createClient(
        "mongodb://jono:G3lassenheit@ds023523.mlab.com:23523/heroku_4f8vnwwf"
    ).getDatabase("heroku_4f8vnwwf").coroutine
}

suspend fun WsSession.existence() = DAO.getExistence(id)
suspend fun WsSession.quidity() = DAO.getQuidity(id)

class Dao {
    /** [Existence._id] -> [Existence] */
    private val existences get() = mongo.getCollection<Existence>()
    /** [WsSession.id] -> <[Existence._id], [Quidity.id]> */
    private val sessions = ConcurrentHashMap<String, Pair<String, String>>()
    /** [WsSession.id] <- [Existence._id] */
    private val exSes = ConcurrentHashMap<String, MutableList<WsSession>>()

    init {
        launch {
            while (true) {
                val ids = mutableListOf<String>()
                existences.distinct(Existence::_id).consumeEach {
                    if (exSes[it]?.isEmpty() == true) ids += it
                }
                existences.updateMany(Existence::_id `in` ids,
                    SetTo(Existence::status, DORMANT))
                delay(1_000 * 60 * 5)
            }
        }
    }

    /** contains [Existence._id]. */
    suspend fun contains(exID: String) =
        existences.findOne(Existence::_id eq exID) != null

    /**
     * Adds a new [WsSession] to an existing [Existence] and returns a new
     * [Quidity] associated with this session.
     */
    suspend fun addSession(
            session: WsSession,
            existence: Existence,
            name: String
    ) : Quidity? {
        return existences.findOne(Existence::_id eq existence._id)?.let {
            if (existence.status == DORMANT) existence.status = DRY
            existence.generateQuidity(name).also {
                existence.enter(it)
                existences.updateOneById(existence._id, existence)
                sessions[session.id] = existence._id to it.id
                exSes.getOrPut(existence._id) { mutableListOf() }.add(session)
            }
        }
    }

    suspend fun newExistence(session: WsSession, existence: Existence)
            : Existence? {
        existences.insertOne(existence).also {
            existence.also {
                sessions[session.id] = existence._id to it.initialQuidity.id
                exSes[existence._id] = mutableListOf(session)
            }
        }
        return existence
    }

    suspend fun getExistence(sessionID: String) =
            sessions[sessionID]?.first?.let {
        existences.findOne(Existence::_id eq it)
    }

    suspend fun getQuidity(sessionID: String) = getExistence(sessionID)?.let {
        it.quidities[sessions[sessionID]!!.second]
    }

    /**
     * Returns the [Existence] matching the [id]. If the [Existence] is dormant,
     * it is moved to the active map.
     */
    suspend fun getEx(id: String) = existences.findOne(Existence::_id eq id)

    fun getSessions(exID: String) = exSes[exID]

    val numSessions get() = sessions.size
    val exList: List<Existence> = runBlocking { existences.find().toList() }
    val dexList: List<Existence> = runBlocking {
        existences.find(Existence::status eq DORMANT).toList()
    }

    suspend fun removeSession(session: WsSession) {
        val (exID, qID) = sessions.remove(session.id)!!
        exSes[exID]?.remove(session)
        val ex = existences.findOneById(exID) ?: return
        if (exSes[exID]?.isEmpty() != false) {
            existences.updateOneById(exID, ex.apply { status = DORMANT })
        }
    }

}

data class StatusPacket(val ex: List<Existence>, val ses: Int)

