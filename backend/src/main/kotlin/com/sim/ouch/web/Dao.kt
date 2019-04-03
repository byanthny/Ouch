package com.sim.ouch.web

import com.sim.ouch.datastructures.*
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quidity
import io.javalin.websocket.WsSession
import io.jsonwebtoken.*
import io.jsonwebtoken.security.SignatureException
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import sun.misc.LRUCache
import java.lang.IllegalArgumentException
import java.time.Instant
import java.util.*
import javax.crypto.KeyGenerator


val DAO: Dao by lazy { Dao() }
private val mongo by lazy {
    KMongoConfiguration.extendedJsonMapper.enableDefaultTyping()
    KMongo.createClient(
        "mongodb://jono:G3lassenheit@ds023523.mlab.com:23523/heroku_4f8vnwwf")
        .getDatabase("heroku_4f8vnwwf").coroutine
}

suspend fun WsSession.existence() = DAO.getExistence(id)
suspend fun WsSession.quidity() = DAO.getQuidity(id)

class Dao {

    /**
     *
     * @property _id The session key used for verification and reconnection
     * @property idPair [Existence._id], [Quidity.id]
     */
    data class SessionData(@BsonId var _id: Key, var idPair: Pair<EC, QC>)

    /** Mongo DB [Existence] collection. */
    private val existences get() = mongo.getCollection<Existence>()
    /** Mongo DB [SessionData] collection */
    private val sessionData = mongo.getCollection<SessionData>()
    /** Key -> [WsSession]? */
    private val keySessionMap = MutableBiMap<Key, WsSession>()
    /** Cache of keys waiting for reconnect. */
    private val disconnectedKeys = ExpiringKache<Key, SessionData?>()

    /**
     * Add an [Existence] to the database, generate a key.
     * `null` if a DB insert failed.
     */
    suspend fun addExistence(
        session: WsSession, existence: Existence
    ) : Triple<Key, Existence, Quidity>? {
        // Generate new Key
        val key = keyGen(session, existence, existence.initialQuidity)
        // Add key to Existence
        existence.addSession(key)
        // Add key to keySessionMap
        
        // Add SessionData & Existence to database
        return Triple(key, existence, existence.initialQuidity)
    }

}

data class StatusPacket(val ex: List<Existence>, val ses: Int)

typealias Key = String
typealias ID = String
typealias EC = ID
typealias QC = ID

fun keyGen(session: WsSession, existence: Existence, quidity: Quidity): Key =
    Jwts.builder().apply {
        setSubject("quidity.${quidity.id}")
        claim("exID", existence._id)
        claim("ip", session.remoteAddress.address.address)
        // setExpiration(Date.from(Instant.now().plusSeconds(60 * 30))) TODO?
        signWith(instanceKey, SignatureAlgorithm.HS256)
    }.compact()

/** Returns `null` on invalid token. */
fun readKey(session: WsSession, token: String) : Pair<EC, QC>? {
    try {
        val claims = tokenParser.parseClaimsJws(token).body
        require(claims["ip"] == session.remoteAddress.address.address)
        require(claims["exID"] is String)
        require(claims.subject.matches(Regex("quidity\\.\\d{10}")))
        return claims["exID"] as String to
            claims.subject.removePrefix("quidity.")
    } catch (e: SignatureException) {
        return null
    } catch (e: ExpiredJwtException) {
        return null
    } catch (e: JwtException) {
        return null
    } catch (e: IllegalArgumentException) {
        return null
    }
}

private val tokenParser get() = Jwts.parser().setSigningKey(instanceKey)!!
private val instanceKey
    by lazy { KeyGenerator.getInstance("HMAC").generateKey()!! }
