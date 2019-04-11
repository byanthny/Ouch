package com.sim.ouch.logic

import at.favre.lib.crypto.bcrypt.BCrypt
import at.favre.lib.crypto.bcrypt.BCrypt.MAX_COST
import com.sim.ouch.web.EC

/**
 * A dataclass holding information on a User and their [Existence]s.
 *
 * @property name
 * @property hash the hashed password
 * @property existences
 */
data class UserData(
    var name: String,
    var hash: CharArray,
    val existences: MutableList<EC> = mutableListOf()
)

fun signup(user: String, password: CharArray): UserData? {
    val hash = BCrypt.withDefaults().hashToChar(MAX_COST, password)

}

fun login(user: String, password: String): UserData? {

}
