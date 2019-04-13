package com.sim.ouch.logic

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.BCrypt.MAX_COST
import com.sim.ouch.IDGenerator
import com.sim.ouch.web.*
import io.javalin.BadRequestResponse
import io.javalin.ConflictResponse
import io.javalin.InternalServerErrorResponse
import io.javalin.NotFoundResponse
import org.bson.codecs.pojo.annotations.BsonId

private val USER_ID_GEN = IDGenerator(16)

/**
 * A dataclass holding information on a User and their [Existence]s.
 *
 * @property name Unique username
 * @property hash the hashed password
 * @property existences a map of [EC] to the user's [QC] in that [Existence].
 * @property _id Bson ID
 */
data class UserData(
    var name: String,
    var hash: CharArray,
    val existences: MutableMap<EC, QC> = mutableMapOf(),
    @BsonId var _id: ID = USER_ID_GEN.next()
)

/**
 * Attempt to create a new [UserData] and store it to mongo.
 *
 * @return The created [UserData]
 * @throws ConflictResponse if the [name] has already been taken.
 */
suspend fun signup(name: String, password: CharArray): UserData {
    // Duplicate name check
    getUserByName(name)?.also { throw ConflictResponse("duplicate name") }
    // Password hash
    val hash = BCrypt.withDefaults().hashToChar(MAX_COST, password)
    // return new user data
    val user = UserData(name, hash)
    if (!saveUser(user)) throw InternalServerErrorResponse("failed to sign-up")
    return user
}

/**
 *
 */
@Throws(NotFoundResponse::class, BadRequestResponse::class)
suspend fun login(name: String, password: String): UserData {
    val user = getUserByName(name) ?: throw NotFoundResponse("no user found")
    val verified = BCrypt.verifyer().verify(password.toCharArray(), user.hash).verified
    if (!verified) throw BadRequestResponse("bad login")
    return user
}
