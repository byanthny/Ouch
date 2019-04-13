package com.sim.ouch.logic

import at.favre.lib.crypto.bcrypt.BCrypt
import com.sim.ouch.*
import com.sim.ouch.web.*
import io.javalin.*
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
    var name: Name,
    var hash: CharArray,
    val existences: MutableMap<EC, QC> = mutableMapOf(),
    @BsonId var _id: ID = USER_ID_GEN.next()
)

fun UserData.sendPacket(token: Token? = null) =
    mutableMapOf("id" to _id, "name" to name, "ex_qd_ids" to existences)
        .also { m -> token?.also { m["token"] = it} }

        /**
 * Attempt to create a new [UserData] and store it to mongo.
 *
 * @return The created [UserData]
 * @throws ConflictResponse if the [name] has already been taken.
 */
suspend fun signup(name: Name, password: CharArray): UserData {
    slog("Starting signup")
    // Duplicate name check
    getUserByName(name)?.also { throw ConflictResponse("duplicate name") }
    slog("hashing password")
    // Password hash
    val hash = BCrypt.withDefaults().hashToChar(16, password)
    slog("building user")
    // return new user data
    val user = UserData(name, hash)
    slog("saving user")
    if (!saveUser(user)) throw InternalServerErrorResponse("failed to sign-up")
    slog("returning $user")
    return user
}

/**
 * Attempt to login and get the [UserData] associated with the [name] and [password].
 * @throws NotFoundResponse if no user was found with the name
 * @throws BadRequestResponse if invalid password
 */
@Throws(NotFoundResponse::class, BadRequestResponse::class)
suspend fun login(name: Name, password: CharArray): UserData {
    val user = getUserByName(name) ?: throw NotFoundResponse("no user found")
    BCrypt.verifyer().verify(password, user.hash)
            .given({!it.verified}) { throw BadRequestResponse("bad login") }
    return user
}
