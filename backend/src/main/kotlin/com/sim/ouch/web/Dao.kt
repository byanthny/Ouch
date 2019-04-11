package com.sim.ouch.web

import com.sim.ouch.IDGenerator
import com.sim.ouch.NOW
import com.sim.ouch.NOW_STR
import com.sim.ouch.Slogger
import com.sim.ouch.avgBy
import com.sim.ouch.datastructures.ExpiringKache
import com.sim.ouch.datastructures.MutableBiMap
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Existence.Status.DORMANT
import com.sim.ouch.logic.PublicExistence
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.logic.UserData
import com.sim.ouch.threadName
import com.sim.ouch.web.logger.Log
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
import org.litote.kmongo.size
import org.litote.kmongo.util.KMongoConfiguration
import java.time.OffsetDateTime
import javax.crypto.KeyGenerator
import javax.crypto.spec.SecretKeySpec
import kotlin.collections.set
import kotlin.reflect.KFunction
import kotlin.reflect.jvm.reflect

typealias Name = String
typealias Token = String
typealias ID = String
/** [Existence] Code ([Existence._id]) */
typealias EC = ID
typealias QC = ID

private val MAX_DORMANT_DAYS = 31L
private val cutoffDate get() = NOW().minusDays(MAX_DORMANT_DAYS)


/**
 * @property _id The session key used for verification and reconnection
 * @property ec [Existence._id]
 * @property qc [Quiddity.id]
 */
data class SessionData(@BsonId var _id: Token, var ec: EC, var qc: QC)

private val mongo by lazy {
    KMongoConfiguration.extendedJsonMapper.enableDefaultTyping()
    launch { do { cleanDB(); delay(1_000 * 60) } while (true) } // TODO
    KMongo.createClient(
            "mongodb://jono:G3lassenheit@ds023523.mlab.com:23523/heroku_4f8vnwwf"
    ).getDatabase("heroku_4f8vnwwf").coroutine
}
private val users get() = mongo.getCollection<UserData>()
/** Mongo DB [Existence] collection. */
private val existences get() = mongo.getCollection<Existence>()
/** Mongo DB [SessionData] collection */
private val sessionData get() = mongo.getCollection<SessionData>()
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

/**
 * Add an [Existence] to the database, generate a token.
 * `null` if a DB insert failed.
 */
suspend fun addExistence(session: WsSession, existence: Existence, qName: String):
        Triple<Token, Existence, Quiddity>? {
    val ini = existence.generateQuidity(qName)
    // Generate new Token
    val token = genToken(session, existence, ini)
    // Add key to Existence
    existence.addSession(token)
    // Add key to sessionTokens
    sessionTokens[token] = session
    // Add SessionData & Existence to database
    val sd = SessionData(token, existence._id, ini.id)
    return when {
        sessionData.save(sd)?.wasAcknowledged() == false -> {
            logger.err("Failed to save new SessionData", ::addExistence)
            null
        }
        existences.save(existence)?.wasAcknowledged() == false -> {
            logger.err("Failed to save new Existence", ::addExistence)
            null
        }
        else -> Triple(token, existence, ini)
    }
}

/**
 * Add a new [WsSession] [Token] to the []. Adds [quiddity] to [existence]
 * Returns the [Token] generated for the session.
 */
suspend fun addSession(session: WsSession, existence: Existence, quiddity: Quiddity):
        Token? {
    // Generate new key
    val token = genToken(session, existence, quiddity)
    // Update Existence
    existence.addSession(token)
    existence.enter(quiddity)
    if (existences.save(existence)?.wasAcknowledged() == false) {
        logger.err("Failed to add session to Existence", ::addSession)
        return null
    }
    // Update sessionData
    val sd = SessionData(token, existence._id, quiddity.id)
    if (sessionData.save(sd)?.wasAcknowledged() == false) {
        logger.err("Failed to save new SessionData", ::addSession)
        return null
    }
    sessionTokens[token] = session
    return token
}

/**
 * Verifies a client [Token] and on success returns the [SessionData] paired
 * to a NEW [Token]. Returns `null` on token validation failure.
 */
suspend fun refreshSession(token: Token, session: WsSession):
        Pair<Token, SessionData>? {
    // Validate Token
    val (exID, qID) = readToken(session, token)
            ?: run {
                logger.err("Token validation failed", ::refreshSession)
                return null
            }
    // Generate new token
    val nToken = genToken(session, exID, qID)
    // Update existence
    existences.findOneById(exID)?.also {
        it.removeSession(token)
        it.addSession(nToken)
        if (!saveExistence(it))
            logger.err("Failed to save Existence", ::refreshSession)
    } ?: run {
        logger.err("Failed to find existence.", ::refreshSession)
        return null
    }
    // Update sessionData
    val sd = SessionData(nToken, exID, qID)
    if (!sessionData.replaceOneById(token, sd).wasAcknowledged()) {
        logger.err("Failed to replace SessionData", ::refreshSession)
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
            ?: return logger.err("No token found", ::disconnect)
    // add token to disconnect
    disconnectedTokens[token] = Unit
    // Remove from existence
    getSessionData(token)?.ec?.let { getExistence(it) }?.also {
        it.removeSession(token)
        if (it.sessionCount == 0) it.status = DORMANT
        if (!saveExistence(it))
            logger.err("Failed to save updated Existence", ::disconnect)
    } ?: logger.err("Failed to remove session from Existence", ::disconnect)
}

/**
 * Save the [Existence] to the database. Use this only when a serialized
 * property is changed.
 *
 * @return `true` if the save was Acknowledged
 */
suspend fun saveExistence(existence: Existence): Boolean {
    return existences.save(existence)?.wasAcknowledged() ?: false
}

suspend fun getUserByName(name: String) =
        users.find(UserData::name eq name).first()

suspend fun getExistence(id: EC) = existences.findOneById(id)
/** Get non-[DORMANT] Existences */
suspend fun getLive() = existences.find(Existence::status ne DORMANT).toList()

/** Get [PublicExistence]. */
suspend fun getPublicExistences() = existences.find(Existence::public eq true)
        .toList().also { il ->
            if (il.isEmpty()) return List(100) { PublicExistence() }
                    .also { existences.insertMany(it) }
        }

/** Get [DORMANT] Existences */
suspend fun getDormant() = existences.find(Existence::status eq DORMANT).toList()

suspend fun getSessionData(token: Token) = sessionData.findOneById(token)
suspend fun getSessionData(session: WsSession) =
        sessionTokens.fromValue(session)?.let { sessionData.findOneById(it) }

fun getToken(session: WsSession) = sessionTokens.fromValue(session)
fun getSession(token: Token) = sessionTokens[token]
val liveSessionsCount get() = sessionTokens.size
suspend fun getLogs() = logger.logCollection.find().toList()
suspend fun status(): StatusPacket {
    val le = getLive()
    val de = getDormant()
    val avgQpe = (le + de).avgBy(Existence::qSize)
    val oldest = (le + de).minBy(Existence::init)?.init
    return StatusPacket(le.size, de.size, liveSessionsCount, avgQpe, oldest)
}

/** Removes old dormant [Existence] */
private suspend fun cleanDB() {
    existences.deleteMany(
            Existence::dormantSince lt cutoffDate).wasAcknowledged().also {
        if (!it)
            logger.err("Failed to delete old dormant existences", ::cleanDB)
    }
    existences.find(
            Existence::sessionTokens size 0,
            Existence::status ne DORMANT
    ).toList().forEach {
        it.status = DORMANT
        if (existences.save(it)?.wasAcknowledged() == false) {
            logger.err("", ::cleanDB)
        }
    }
}

data class StatusPacket(
        val numLiveEx: Int,
        val numDormEx: Int,
        val numLiveSes: Int,
        val avgQpe: Double,
        val oldest: OffsetDateTime?
)

private fun genToken(session: WsSession, existence: Existence, quiddity: Quiddity) =
        genToken(session, existence._id, quiddity.id)

private fun genToken(session: WsSession, exID: EC, qID: QC) = Jwts.builder().apply {
    setSubject("quiddity.$qID").claim("exID", exID)
    // claim("ip", session.remoteAddress.address.address)
    // setExpiration(Date.from(Instant.now().plusSeconds(60 * 30))) TODO?
    signWith(instanceKey, SignatureAlgorithm.HS256)
}.compact()

/** Returns `null` on invalid token. */
private fun readToken(session: WsSession, token: String): Pair<EC, QC>? {
    try {
        val claims = tokenParser.parseClaimsJws(token).body
        // require(claims["ip"] == session.remoteAddress.address.address)
        require(claims["exID"] is String)
        require(claims.subject.matches(Regex("quiddity\\.([\\dA-Z]){10}")))
        return claims["exID"] as String to
                claims.subject.removePrefix("quiddity.")
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

private data class Key(val key: ByteArray)

private val instanceKey by lazy {
    runBlocking {
        mongo.getCollection<Key>().let { c ->
            c.find().first()?.let { SecretKeySpec(it.key, "HMACSHA256") }
                    ?: let {
                        logger.err("Failed to load key. Loading new key", ::readToken)
                        KeyGenerator.getInstance("HMACSHA256").generateKey()
                                ?.also { it.encoded.let { c.insertOne(Key(it)) } }
                    }
        }
    }
}

suspend fun WsSession.existence(): Existence? = getToken(this)
        ?.let { token -> getSessionData(token)?.let { getExistence(it.ec) } }

suspend fun WsSession.quidity(): Quiddity? = getToken(this)
        ?.let { t -> getSessionData(t) }
        ?.let { (ec, qc) -> getExistence(ec)?.qOf(qc) }


/** A [Slogger] which saves a [Log] to the `logs` logCollection collection. */
object logger : Slogger("") {
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

    suspend fun <F : KFunction<*>> info(any: Any? = "", function: F? = null) =
            log(any, function, "info")

    suspend fun <F : KFunction<*>> err(any: Any? = "", function: F? = null) =
            log(any, function, "err")

    private suspend fun <F : KFunction<*>> log(
            any: Any? = "",
            f: F?,
            level: String
    ) {
        println(log("[${NOW_STR()}] [$threadName] [$name] ${
        f?.let { "[${f.name}]" } ?: ""} [$level] $any"))
        if (log.size % 10 == 0) {
            println("Updating Log Collection...")
            if (logCollection.save(log)?.wasAcknowledged() == true) {
                println("\tSuccessful")
            } else System.err.println("\tFailed!")
        }
    }
}
