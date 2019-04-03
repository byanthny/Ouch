package com.sim.ouch.web

import com.sim.ouch.*
import com.sim.ouch.datastructures.ExpiringKache
import com.sim.ouch.datastructures.MutableBiMap
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Existence.Status.DORMANT
import com.sim.ouch.logic.Quidity
import com.sim.ouch.web.Dao.DaoLogger.Log
import io.javalin.websocket.WsSession
import io.jsonwebtoken.ExpiredJwtException
import io.jsonwebtoken.JwtException
import io.jsonwebtoken.Jwts
import io.jsonwebtoken.SignatureAlgorithm
import io.jsonwebtoken.security.SignatureException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.`in`
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.eq
import org.litote.kmongo.lt
import org.litote.kmongo.ne
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.time.OffsetDateTime
import javax.crypto.KeyGenerator
import kotlin.collections.set
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.reflect


val DAO: Dao by lazy { Dao() }
private val mongo by lazy {
    KMongoConfiguration.extendedJsonMapper.enableDefaultTyping()
    KMongo.createClient(
        "mongodb://jono:G3lassenheit@ds023523.mlab.com:23523/heroku_4f8vnwwf")
        .getDatabase("heroku_4f8vnwwf").coroutine
}

suspend fun WsSession.existence() = DAO.getToken(this)
    ?.let { token -> DAO.getSessionData(token)
        ?.let { DAO.getExistence(it.ec) } }
suspend fun WsSession.quidity() = DAO.getToken(this)
    ?.let { t -> DAO.getSessionData(t) }
    ?.let { (ec, qc) -> DAO.getExistence(ec)?.let { e -> e.quidities[qc] } }

class Dao {

    /**
     * @property _id The session key used for verification and reconnection
     * @property ec [Existence._id]
     * @property qc [Quidity.id]
     */
    data class SessionData(@BsonId var _id: Token, var ec: EC, var qc: QC)

    private val logger = DaoLogger()
    /** Mongo DB [Existence] collection. */
    private val existences get() = mongo.getCollection<Existence>()
    /** Mongo DB [SessionData] collection */
    private val sessionData = mongo.getCollection<SessionData>()
    /** Token -> [WsSession]? */
    private val sessionTokens = MutableBiMap<Token, WsSession>()
    /**
     * Cache of keys waiting for reconnect. Removes [SessionData] from
     * [sessionData] on trashing.
     */
    private val disconnectedTokens = ExpiringKache<Token, Unit?> { m ->
        runBlocking {
            val a = sessionData.deleteMany(SessionData::_id `in` m.keys)
            if (!a.wasAcknowledged())
                logger.err(
                    "Failed to delete expired disconnected token",
                    ExpiringKache<*, *>::trashAction.reflect()
                )
        }
    }
    private val MAX_DORMANT_DAYS = 31L
    private val cutoffDate get() = NOW().minusDays(MAX_DORMANT_DAYS)

    init {
        launch { while (true) { delay(1_000 * 60 * 30); cleanDB() } }
    }

    /**
     * Add an [Existence] to the database, generate a token.
     * `null` if a DB insert failed.
     */
    suspend fun addExistence(session: WsSession, existence: Existence)
            : Triple<Token, Existence, Quidity>? {
        val ini = existence.initialQuidity
        // Generate new Token
        val key = genToken(session, existence, ini)
        // Add key to Existence
        existence.addSession(key)
        // Add key to sessionTokens
        sessionTokens[key] = session
        // Add SessionData & Existence to database
        val sd = SessionData(key, existence._id, ini.id)
        return when {
            sessionData.save(sd)?.wasAcknowledged() == false -> {
                logger.err("Failed to save new SessionData", Dao::addExistence)
                null
            }
            existences.save(existence)?.wasAcknowledged() == false -> {
                logger.err("Failed to save new Existence", Dao::addExistence)
                null
            }
            else -> Triple(key, existence, ini)
        }
    }

    /**
     * Add a new [WsSession] and [Token] to the [Dao].
     * Returns the [Token] generated for the session.
     */
    suspend fun addSession(session: WsSession, existence: Existence, quidity: Quidity)
            : Token? {
        // Generate new key
        val token = genToken(session, existence, quidity)
        // Update Existence
        existence.addSession(token)
        existence.quidities[quidity.id] = quidity
        if (existences.save(existence)?.wasAcknowledged() == false) {
            logger.err("Failed to add session to Existence", Dao::addSession)
            return null
        }
        // Update sessionData
        val sd = SessionData(token, existence._id, quidity.id)
        if (sessionData.save(sd)?.wasAcknowledged() == false) {
            logger.err("Failed to save new SessionData", Dao::addSession)
            return null
        }
        sessionTokens[token] = session
        return token
    }

    /**
     * Verifies a client [Token] and on success returns the [SessionData] paired
     * to a NEW [Token]. Returns `null` on token validation failure.
     */
    suspend fun refreshSession(token: Token, session: WsSession)
            : Pair<Token, SessionData>? {
        // Validate Token
        val (exID, qID) = readToken(session, token) ?: return null
        // Generate new token
        val nToken = genToken(session, exID, qID)
        // Update existence
        existences.findOneById(exID)?.also {
            it.addSession(token)
            existences.save(it)
        } ?: let {
            logger.err("", Dao::refreshSession)
            return null
        }
        // Update sessionData
        val sd = SessionData(nToken, exID,  qID)
        if (!sessionData.replaceOneById(token, sd).wasAcknowledged()) {
            logger.err("Failed to replace SessionData", Dao::refreshSession)
            return null
        }
        // Update SessionToken
        sessionTokens[token] = session
        return nToken to sd
    }

    /**
     * On session close, remove the [session] from from all active collections,
     * and move the token to the [disconnectedTokens].
     */
    suspend fun disconnect(session: WsSession) {
        // Remove token from sessionTokens
        val token = sessionTokens.removeValue(session)
            ?: return logger.err("No token found", Dao::disconnect)
        // add token to disconnect
        disconnectedTokens[token] = unit
        // Remove from existence
        getSessionData(token)?.ec?.let { getExistence(it) }?.also {
            it.removeSession(token)
            if (existences.save(it)?.wasAcknowledged() == false)
                logger.err("Failed to save updated Existence", Dao::disconnect)
        } ?: logger.err("Failed to remove session from Existence", Dao::disconnect)
    }

    suspend fun getExistence(id: EC) = existences.findOneById(id)
    /** Get non-[DORMANT] Existences */
    suspend fun getLive() = existences.find(Existence::status ne DORMANT).toList()
    /** Get [DORMANT] Existences */
    suspend fun getDormant() = existences.find(Existence::status eq DORMANT).toList()
    suspend fun getSessionData(token: Token) = sessionData.findOneById(token)
    suspend fun getSessionData(session: WsSession) =
        sessionTokens.fromValue(session)?.let { sessionData.findOneById(it) }
    fun getToken(session: WsSession) = sessionTokens.fromValue(session)
    fun getSession(token: Token) = sessionTokens[token]
    val liveSessionsCount get() = sessionTokens.size
    suspend fun getLogs() = logger.logCollection.find().toList()

    /** Removes old dormant [Existence] */
    private suspend fun cleanDB(): Boolean {
        return existences.deleteMany(
            Existence::dormantSince lt cutoffDate).wasAcknowledged().also {
            if (!it) logger.err(
                "Failed to delete old dormant existences", Dao::cleanDB
                )
        }
    }

    /** A [Slogger] which saves a [Log] to the `logs` logCollection collection. */
    class DaoLogger : Slogger("DAO") {
        data class Log(
            @BsonId val _id: ID = next(),
            val loog: MutableList<String> = mutableListOf(),
            val date: OffsetDateTime = OffsetDateTime.now()
        ) {
            companion object idGen : IDGenerator(10)
            operator fun invoke(string: String) = string.also { loog.add(it) }
            val size get() = loog.size
        }
        val logCollection by lazy { mongo.getCollection<Log>() }
        private val log: Log = Log()

        suspend fun <F: KFunction<*>> info(any: Any? = "", function: F? = null) =
            log(any, function, "info")
        suspend fun <F: KFunction<*>> err(any: Any? = "", function: F? = null) =
            log(any, function, "err")


        private suspend fun <F: KFunction<*>> log(
            any: Any? = "", f: F?, level: String
        ) {
            println(log("[${NOW_STR()}] [$threadName] [$name] ${
            f?.let { "[${f.name}]" } ?: ""} [$level] $any"))
            if (log.size % 100 == 0) logCollection.save(log)
        }
    }

}

data class StatusPacket(val ex: List<Existence>, val ses: Int)

typealias Token = String
typealias ID = String
/** [Existence] Code ([Existence._id]) */ typealias EC = ID
typealias QC = ID

private fun genToken(session: WsSession, existence: Existence, quidity: Quidity) =
    genToken(session, existence._id, quidity.id)

private fun genToken(session: WsSession, exID: EC, qID: QC) = Jwts.builder().apply {
    setSubject("quidity.$qID").claim("exID", exID)
    claim("ip", session.remoteAddress.address.address)
    // setExpiration(Date.from(Instant.now().plusSeconds(60 * 30))) TODO?
    signWith(instanceKey, SignatureAlgorithm.HS256)
}.compact()

/** Returns `null` on invalid token. */
fun readToken(session: WsSession, token: String) : Pair<EC, QC>? {
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
