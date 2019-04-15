package com.sim.ouch.web

import com.sim.ouch.*
import com.sim.ouch.datastructures.ExpiringKache
import com.sim.ouch.datastructures.MutableBiMap
import com.sim.ouch.logic.*
import com.sim.ouch.logic.Existence.Status.DORMANT
import io.javalin.websocket.WsSession
import io.jsonwebtoken.*
import io.jsonwebtoken.security.SignatureException
import kotlinx.coroutines.delay
import kotlinx.coroutines.runBlocking
import org.bson.codecs.pojo.annotations.BsonId
import org.litote.kmongo.*
import org.litote.kmongo.coroutine.coroutine
import org.litote.kmongo.reactivestreams.KMongo
import org.litote.kmongo.util.KMongoConfiguration
import java.time.Instant
import java.time.OffsetDateTime
import java.time.temporal.ChronoUnit.MINUTES
import java.util.*
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

private const val MAX_DORMANT_DAYS = 31L
private val cutoffDate get() = NOW().minusDays(MAX_DORMANT_DAYS)

/******* Mongo Collections *******/
/**
 * @property _id The session key used for verification and reconnection
 * @property ec [Existence._id]
 * @property qc [Quiddity.id]
 */
data class SessionData(@BsonId var _id: Token, var ec: EC, var qc: QC)
private fun sessionOf(token: Token, ec: EC, qc: QC) = SessionData(token, ec, qc)

val clean: () -> Unit = {
    runBlocking {
        existences.deleteMany(Existence::dormantSince lt cutoffDate)
            .given({ it.wasAcknowledged() }) {
                err("Failed to delete old dormant existences", null)
            }
        existences.find(Existence::sessionTokens size 0,
            Existence::status ne DORMANT).toList().forEach {
            it.status = DORMANT
            if (existences.save(it)?.wasAcknowledged() == false) {
                err("", null)
            }
        }
    }
}

private val mongo by lazy {
    KMongoConfiguration.extendedJsonMapper.enableDefaultTyping()
    KMongo.createClient(
            "mongodb://jono:G3lassenheit@ds023523.mlab.com:23523/heroku_4f8vnwwf"
    ).getDatabase("heroku_4f8vnwwf").coroutine
            .also {
                launch {
                    do {
                        clean(); delay(1_000 * 60)
                    } while (true)
                } // TODO
            }
}
/** Mongo DB [UserData] collection. */
private val users get() = mongo.getCollection<UserData>()
/** Mongo DB [Existence] collection. */
private val existences get() = mongo.getCollection<Existence>()
/** Mongo DB [SessionData] collection */
private val sessionData get() = mongo.getCollection<SessionData>()
/** Token <-> [WsSession]? */
private val sessionTokens = MutableBiMap<Token, WsSession>()

/**
 * Cache of keys waiting for reconnect. Removes [SessionData] from
 * [sessionData] on trashing.
 */
private val disconnectedTokens = ExpiringKache<Token, Unit?> { m ->
    runBlocking { sessionData.deleteMany(SessionData::_id `in` m.keys) }
        .given({ !it.wasAcknowledged() }) {
        runBlocking {
            err("Failed to delete expired disconnected token",
            ExpiringKache<*, *>::trashAction.reflect())
        }
    }
}

suspend fun getUserByName(name: String) = users.find(UserData::name eq name).first()
suspend fun getUserById(id: ID) = users.findOneById(id)

suspend fun saveUser(userData: UserData) =
    users.save(userData)?.wasAcknowledged() ?: false

suspend fun getUsers() = users.find().toList()

/**
 * Add an [Existence] to the database, generate a token.
 * `null` if a DB insert failed.
 */
suspend fun addExistence(user: UserData, session: WsSession,
                         existence: Existence,
                         qName: String): Triple<Token, Existence, Quiddity>? {
    val ini = existence.generateQuidity(qName)
    // Generate new Token
    val token = reconnectTokenOf(user, existence, ini)
    // Add key to Existence
    existence.addSession(token)
    // Add key to sessionTokens
    sessionTokens[token] = session
    // Add SessionData & Existence to database
    val sd = SessionData(token, existence._id, ini.id)
    return when {
        sessionData.save(sd)?.wasAcknowledged() == false -> {
            err("Failed to save new SessionData", ::addExistence).nil
        }
        existences.save(existence)?.wasAcknowledged() == false -> {
            err("Failed to save new Existence", ::addExistence).nil
        }
        else -> Triple(token, existence, ini)
    }
}

/**
 * Add a new [WsSession] [Token] to the []. Adds [qd] to [ex]
 * Returns the [Token] generated for the session.
 */
suspend fun addSession(user: UserData, session: WsSession, ex: Existence,
                       qd: Quiddity): Token? {
    // Generate new key
    val token = reconnectTokenOf(user, ex, qd)
    // Update Existence
    ex.addSession(token)
    ex.enter(qd)
    return when {
        existences.save(ex)?.wasAcknowledged() == false ->
            err("Failed to add session to Existence", ::addSession).nil
        sessionData.save(sessionOf(token, ex._id, qd.id))?.wasAcknowledged() == false ->
            err("Failed to save new SessionData", ::addSession).nil
        else -> token.also { sessionTokens[it] = session }
    }
}

/**
 * Verifies a client [Token] and on success returns the [SessionData] paired
 * to a NEW [Token]. Returns `null` on token validation failure.
 */
suspend fun Token.refreshWith(user: UserData, session: WsSession):
        Pair<Token, SessionData>? {
    // Validate Token
    val (exID, qID) = try {
        readReconnect()
    } catch (e: Exception) {
        return err("Token validation failed: ${e.message}", ::refreshWith).nil
    }
    // Generate new token
    val nToken = reconnectTokenOf(user, exID, qID)
    // Update existence
    existences.findOneById(exID)?.also {
        it.removeSession(this)
        it.addSession(nToken)
        if (!saveExistence(it)) err("Failed to save Existence", ::refreshWith)
    } ?: return err("Failed to find existence.", ::refreshWith).nil

    // Update sessionData
    val sd = SessionData(nToken, exID, qID)
    if (!sessionData.replaceOneById(this, sd).wasAcknowledged()) {
        return err("Failed to replace SessionData", ::refreshWith).nil
    }
    // Update SessionToken
    sessionTokens[this] = session
    return nToken to sd
}

/**
 * On session close, remove the [session] from from all active collections,
 * and move the token to the [disconnectedTokens].
 */
suspend fun disconnect(session: WsSession) {
    // Remove token from sessionTokens
    val token = sessionTokens.removeValue(session)
            ?: return err("No token found", ::disconnect)
    // add token to disconnect
    disconnectedTokens[token] = Unit
    // Remove from existence
    getSessionData(token)?.ec?.let { getExistence(it) }?.also {
        it.removeSession(token)
        if (it.sessionCount == 0) it.status = DORMANT
        if (!saveExistence(it)) err("Failed to save updated Existence", ::disconnect)
    } ?: err("Failed to remove session from Existence", ::disconnect)
}

/**
 * Save the [Existence] to the database. Use this only when a serialized
 * property is changed.
 *
 * @return `true` if the save was Acknowledged
 */
suspend fun saveExistence(existence: Existence) =
        existences.save(existence)?.wasAcknowledged() ?: false

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

data class StatusPacket(
    val numLiveEx: Int,
    val numDormEx: Int,
    val numLiveSes: Int,
    val avgQpe: Double,
    val oldest: OffsetDateTime?
)

suspend fun status(): StatusPacket {
    val le = getLive()
    val de = getDormant()
    val avgQpe = (le + de).avgBy(Existence::qSize)
    val oldest = (le + de).minBy(Existence::init)?.init
    return StatusPacket(le.size, de.size, liveSessionsCount, avgQpe, oldest)
}

/** Removes old dormant [Existence] */


/********** Tokens *****************/

/** Generate a JWT token for REST authentication. */
fun authTokenOf(userData: UserData) = Jwts.builder().addClaims(
    mapOf("ex_map" to userData.existences, "type" to "auth")).setSubject(
        userData._id).claim("name", userData.name).setExpiration(
        Date.from(Instant.now().plus(30, MINUTES))).signWith(instanceKey,
        SignatureAlgorithm.HS256).compact()!!

@Throws(
    ExpiredJwtException::class, UnsupportedJwtException::class,
    MalformedJwtException::class, SignatureException::class,
    IllegalArgumentException::class
)
suspend fun Token.readAuth(): UserData? {
    val claims = tokenParser.parseClaimsJws(this).body
    val id = claims.subject
    require(claims["type"] == "auth")
    return getUserById(id)
}

/** Generate a JWT token for reconnecting to a Websocket. */
private fun reconnectTokenOf(userData: UserData,
    existence: Existence,
    quiddity: Quiddity) = reconnectTokenOf(userData, existence._id, quiddity.id)

/** Generate a JWT token for reconnecting to a Websocket. */
private fun reconnectTokenOf(user: UserData, exID: EC,
                             qID: QC): Token = Jwts.builder().setSubject(
    user._id).claim("qc", qID).claim("ec", exID).claim("type",
        "recon").setExpiration(
        Date.from(Instant.now().plusSeconds(60 * 5))).signWith(instanceKey,
        SignatureAlgorithm.HS256).compact()

/** Returns `null` on invalid token. */
@Throws(ExpiredJwtException::class, UnsupportedJwtException::class,
    MalformedJwtException::class, SignatureException::class,
    IllegalArgumentException::class)
fun Token.readReconnect(): Pair<EC, QC> {
    val claims = tokenParser.parseClaimsJws(this).body
    require(claims["ec"] is EC)
    require(claims["qc"] is QC)
    require(claims["type"] == "recon")
    return claims["ec"] as String to claims["qc"] as String
}


private val tokenParser get() = Jwts.parser().setSigningKey(instanceKey)!!

private data class Key(val key: ByteArray)

private val instanceKey by lazy {
    runBlocking {
        mongo.getCollection<Key>().let { c ->
            c.find().first()?.let { SecretKeySpec(it.key, "HMACSHA256") }
                    ?: let {
                        err("Failed to load key. Loading new key", Token::readReconnect)
                        KeyGenerator.getInstance("HMACSHA256").generateKey()
                                ?.also { it.encoded.let { c.insertOne(Key(it)) } }
                    }
        }
    }
}


/****** Logging *******/

data class Log internal constructor(@BsonId val _id: ID = next()) {
    val size get() = loog.size
    val loog: MutableList<String> = mutableListOf()
    val date: OffsetDateTime = OffsetDateTime.now()
    operator fun invoke(string: String) = string.also { loog.add(it) }
    companion object idGen : IDGenerator(10)
}

private val logCollection by lazy { mongo.getCollection<Log>() }
/** Get [Log]s from mongo */
suspend fun getLogs() = logCollection.find().toList()

/** The [Log] used for this run of the program. */
private val instanceLog: Log = Log()

private suspend fun <F : KFunction<*>> info(any: Any? = "", function: F? = null) =
    log(any, function, "info")

private suspend fun <F : KFunction<*>> err(any: Any? = "", function: F? = null) =
    log(any, function, "err")

private suspend fun <F : KFunction<*>> log(any: Any? = "", f: F?, level: String) {
    println(instanceLog("[${NOW_STR()}] [$threadName] [DAO] ${
    f?.let { "[${f.name}]" } ?: ""} [$level] $any"))
    if (instanceLog.size % 10 == 0) {
        println("Updating Log Collection...")
        if (logCollection.save(instanceLog)?.wasAcknowledged() == true) {
            println("\tSuccessful")
        } else System.err.println("\tFailed!")
    }
}
