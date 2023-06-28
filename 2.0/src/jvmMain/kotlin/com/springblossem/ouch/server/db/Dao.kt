package com.springblossem.ouch.server.db

import com.springblossem.ouch.common.Existence
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun connectDB(): Database {
  val db = Database.connect(
    url = System.getenv("SQL_URI"),
    user = System.getenv("SQL_USER"),
    password = System.getenv("SQL_PASSWORD"),
    databaseConfig = DatabaseConfig {
      useNestedTransactions = true
    }
  )
  transaction {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(Existences)
  }
  return db
}

private object Existences : IntIdTable() {

  val name = varchar("name", 50)
  val public = bool("public").default(true)
  val capacity = integer("capacity").nullable()
  val createdAt = long("createdAt")
  val dormantAt = long("dormantAt").nullable()
}

/** Map a query result to a list of [Existence]s */
fun Query.toExistences(): List<Existence> = map {
  Existence(
    id = it[Existences.id].value,
    name = it[Existences.name],
    createdAt = it[Existences.createdAt],
    public = it[Existences.public],
    capacity = it[Existences.capacity],
    dormantAt = it[Existences.dormantAt]
  )
}

fun Existence.Companion.new(
  name: String,
  createdAt: Long,
  public: Boolean = true,
  capacity: Int? = null,
): Int = transaction {
  Existences.insertAndGetId {
    it[Existences.name] = name
    it[Existences.createdAt] = createdAt
    it[Existences.public] = public
    it[Existences.capacity] = capacity
  }.value
}
