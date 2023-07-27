package com.sim.ouch.web

import com.sim.ouch.*
import com.sim.ouch.datastructures.MutableBiMap
import com.sim.ouch.logic.Existence
import com.sim.ouch.logic.Quiddity
import com.sim.ouch.web.DaoLogger.Log
import io.javalin.websocket.WsContext
import io.jsonwebtoken.*
import io.jsonwebtoken.security.SignatureException
import korlibs.time.DateFormat
import korlibs.time.DateTime
import korlibs.time.ISO8601
import korlibs.time.days
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable
import javax.crypto.spec.SecretKeySpec
import kotlin.reflect.KFunction

/**
 * The [SessionInfo] class is used to hold information about a single Websocket
 * connection. The joined [Existence] and related [Quiddity] are held by this class.
 *
 * @property token The session key used for verification and reconnection
 * @property ec [Existence.id]
 * @property qc [Quiddity.id]
 */
@Serializable
data class SessionInfo(
  @SerialName("token") var token: Token,
  var ec: EC,
  var qc: QC
)

private val cutoffDate
  get() = DateTime.now().plus(OuchInfo.Settings.max_dormant_age.days)

/** [Token] -> [WsContext]? */
private val sessionTokens = MutableBiMap<Token, WsContext>()

/** Returns the [WsContext] associated with this [Token] if on exists. */
val Token.wsContext get() = sessionTokens[this]

/** Returns the [SessionInfo] associated with this [Token]. */
val Token.sessionInfo get() = Dao.sessions[this]

/** Returns the [Token] associated with this [WsContext] if on exists. */
val WsContext.token get() = sessionTokens.fromValue(this)

val WsContext.info get() = token?.sessionInfo

val WsContext.existence get() = info?.run { getExistence(ec) }

val WsContext.quiddity get() = info?.run { existence?.qOf(qc) }

// //////////////////////
//  Session Creation  //
// /////////////////////

/**
 * [Enters][Existence.enter] a new [Quiddity] into this [Existence].
 *
 * @param name The name of the [Quiddity].
 * @param context
 * @return The generated [Token] and [Quiddity].
 */
fun Existence.join(name: Name, context: WsContext): Pair<Token, Quiddity> {
  val q = generateQuidity(name)
  val tkn = "" // genToken(this, q) todo
  GlobalScope.launch { SessionInfo(tkn, this@join.id, q.id).insert(context) }
  return tkn to q
}

/**
 * @param context
 * @return This [Token]'s associated [SessionInfo] and a new token. `null` if token
 * validation failed.
 */
fun Token.refresh(): Pair<SessionInfo, Token>? {
  val (ec, qc) = readToken(this) ?: return null
  val ntkn = genToken(ec, qc)
  return sessionInfo?.let { it to ntkn }
}

/**
 * @return The first non-full [public][Existence.public] existence. If one is not
 * found, one will be created, if the creation fails `null` will be returned.
 */
suspend fun getNextPublicExistence(): Existence? = getPublicExistences()
  .values
  .firstOrNull { !it.full }
  ?: existence(true)

/** Create a new [Existence] and save it to [Dao.existences]. */
suspend fun existence(public: Boolean = false): Existence? {
  val e = if (public) Existence.newPublic else Existence.newDefault
  Dao.existences[e.id] = e
  DaoLogger.info("New Existence ${e.id}", null)
  return e
}

// /////////////////////
//  Session Removal //
// ////////////////////

suspend fun WsContext.remove() {
  token?.dormant()
  session.close()
}

suspend fun Token.dormant() {
}

// /////////////////////
//  Data Accessors  //
// ////////////////////

/** TODO Dao backup & delete queues */
private object Dao {

  /** Mongo DB [Existence] collection. */
  val existences = mutableMapOf<EC, Existence>()

  /** Mongo DB [SessionInfo] collection */
  val sessions = mutableMapOf<Token, SessionInfo>()
}

private suspend fun SessionInfo.insert(context: WsContext) {
  sessionTokens[token] = context
  Dao.sessions[this.token] = this
}

suspend fun Existence.modify(scope: suspend Existence.() -> Unit) {
  apply { scope(this) }
  // TODO backup queue
  Dao.existences[this.id] = this
}

/** Returns the [Existence] with the given [ec] if one is found. */
fun getExistence(ec: EC): Existence? = Dao.existences[ec]

suspend fun getAllExistences(): Map<EC, Existence> = Dao.existences

suspend fun getPublicExistences(): Map<EC, Existence> =
  Dao.existences.filterValues { it.public }

suspend fun logs() = listOf<String>()

// /////////////////////
// TOKEN GENERATION //
// ////////////////////

@Serializable
private class Key(val key: ByteArray) {

  override fun equals(other: Any?) = other is Key &&
      other.key.contentEquals(this.key)

  override fun hashCode() = key.hashCode()

  companion object {

    private val HMAC_SHA = "HMACSHA256"
    private val key = byteArrayOf()

    fun get() = SecretKeySpec(key, HMAC_SHA)
  }
}

private val Token.claims: Claims
  get() = Jwts.parser().setSigningKey(Key.get())!!.parseClaimsJws(this).body

private fun genToken(existence: Existence, quiddity: Quiddity) =
  genToken(existence.id, quiddity.id)

private fun genToken(exID: EC, qID: QC) = Jwts.builder().apply {
  signWith(Key.get(), SignatureAlgorithm.HS256)
  setSubject("quiddity.$qID")
  claim("exID", exID)
}.compact()

/** Returns `null` on invalid token. */
private fun readToken(token: String): Pair<EC, QC>? = try {
  val claims = token.claims
  require(claims["exID"] is String)
  require(claims.subject.matches(Regex("quiddity\\.([\\dA-Z]){10}")))
  claims["exID"] as String to
      claims.subject.removePrefix("quiddity.")
} catch (e: SignatureException) {
  null
} catch (e: ExpiredJwtException) {
  null
} catch (e: JwtException) {
  null
} catch (e: IllegalArgumentException) {
  null
}

// /////////////////////
//      Logger      //
// ////////////////////

/** A [Slogger] which saves a [Log] to the `logs` logCollection collection. */
object DaoLogger : Slogger("") {

  @Serializable
  data class Log(
    @SerialName("id") val id: ID = IDGenerator.nextDefault,
    val entries: MutableList<String> = mutableListOf(),
    val dateISO: String = DateTime.now().format(ISO8601.DATETIME_COMPLETE)
  ) {

    val size get() = entries.size
    operator fun invoke(string: String) = string.also { entries.add(it) }
  }

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
    println(
      log(buildString {
        append('[').append(
          DateTime.now().format(DateFormat.DEFAULT_FORMAT)
        )
        append("] ")
        append('[').append(threadName).append("] ")
        append('[').append(name).append("] ")
        if (f != null) append('[').append(f.name).append("] ")
        append('[').append(level).append("] ")
        append(any)
      })
    )
  }
}

@Serializable
data class StatusPacket(
  val numLiveEx: Int,
  val numDormEx: Int,
  val numLiveSes: Int,
  val avgQpe: Double
)

suspend fun status(): StatusPacket =
  getAllExistences().let { ae ->
    StatusPacket(
      ae.size,
      ae.count { it.value.status == Existence.Status.DORMANT },
      sessionTokens.size,
      ae.map { it.value.qSize }.average()
    )
  }
