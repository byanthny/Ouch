package com.springblossem.ouch.server.db

import com.springblossem.ouch.common.Auth
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.select
import org.jetbrains.exposed.sql.transactions.transaction

object AuthTable : IntIdTable() {

  val username = varchar("username", 50).uniqueIndex()
  val hash = varchar("hash", 200)
}

fun Query.toAuths(): List<Auth> = map {
  Auth(
    id = it[AuthTable.id].value,
    username = it[AuthTable.username],
    hash = it[AuthTable.hash]
  )
}

fun Auth.Companion.new(username: String, hash: String): Int = transaction {
  AuthTable.insertAndGetId {
    it[AuthTable.username] = username
    it[AuthTable.hash] = hash
  }
}.value

operator fun Auth.Companion.get(id: Int): Auth? = transaction {
  AuthTable
    .select { AuthTable.id eq id }
    .limit(1)
    .toAuths()
    .firstOrNull()
}

operator fun Auth.Companion.get(username: String): Auth? = transaction {
  AuthTable
    .select { AuthTable.username eq username }
    .limit(1)
    .toAuths()
    .firstOrNull()
}
