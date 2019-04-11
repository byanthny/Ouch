package com.sim.ouch.logic

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.BCrypt.MAX_COST
import com.sim.ouch.IDGenerator
import com.sim.ouch.web.DAO
import com.sim.ouch.web.EC
import com.sim.ouch.web.ID
import com.sim.ouch.web.QC
import io.javalin.ConflictResponse
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

suspend fun signup(user: String, password: CharArray): UserData? {
    DAO.getUserByName(user)?.also { throw ConflictResponse("duplicate name") }
    val hash = BCrypt.withDefaults().hashToChar(MAX_COST, password)
    return UserData(user, hash)
}

fun login(user: String, password: String): UserData? {

}
