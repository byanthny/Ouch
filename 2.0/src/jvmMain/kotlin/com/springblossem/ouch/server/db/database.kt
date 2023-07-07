package com.springblossem.ouch.server.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun connectDB(
  url: String? = null,
  user: String? = null,
  password: String? = null
): Database {
  val db = Database.connect(
    url = url ?: System.getenv("SQL_URI"),
    user = user ?: System.getenv("SQL_USER"),
    password = password ?: System.getenv("SQL_PASSWORD"),
    databaseConfig = DatabaseConfig {
      useNestedTransactions = true
    })
  transaction {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(ExistenceTable, AuthTable)
  }
  return db
}

