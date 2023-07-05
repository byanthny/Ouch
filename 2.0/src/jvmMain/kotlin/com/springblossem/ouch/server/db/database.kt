package com.springblossem.ouch.server.db

import org.jetbrains.exposed.sql.*
import org.jetbrains.exposed.sql.transactions.transaction

fun connectDB(): Database {
  val db = Database.connect(
    url = System.getenv("SQL_URI"),
    user = System.getenv("SQL_USER"),
    password = System.getenv("SQL_PASSWORD"),
    databaseConfig = DatabaseConfig {
      useNestedTransactions = true
    })
  transaction {
    addLogger(StdOutSqlLogger)
    SchemaUtils.create(ExistenceTable, AuthTable)
  }
  return db
}

