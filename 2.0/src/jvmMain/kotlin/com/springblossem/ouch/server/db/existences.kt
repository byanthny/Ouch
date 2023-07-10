package com.springblossem.ouch.server.db

import com.springblossem.ouch.common.Existence
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.Query
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.transactions.transaction

object ExistenceTable : IntIdTable() {

  val public = bool("public").default(true)
  val capacity = integer("capacity").nullable()
  val createdAt = long("createdAt")
  val dormantAt = long("dormantAt").nullable()
}

/** Map a query result to a list of [Existence]s */
fun Query.toExistences(): List<Existence> = map {
  Existence(
    id = it[ExistenceTable.id].value,
    createdAt = it[ExistenceTable.createdAt],
    public = it[ExistenceTable.public],
    capacity = it[ExistenceTable.capacity],
    dormantAt = it[ExistenceTable.dormantAt]
  )
}

fun Existence.Companion.new(
  createdAt: Long,
  public: Boolean = true,
  capacity: Int? = null,
): Int = transaction {
  ExistenceTable.insertAndGetId {
    it[ExistenceTable.createdAt] = createdAt
    it[ExistenceTable.public] = public
    it[ExistenceTable.capacity] = capacity
  }
}.value

