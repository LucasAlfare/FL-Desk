package com.lucasalfare.fldesk.database

import com.lucasalfare.flbase.database.AppDB
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedDatabase {

  private val sqliteUrl = "jdbc:sqlite:data.db"
  private val sqliteDriver = "org.sqlite.JDBC"

  private var initialized = false

  fun initialize(forceInitialization: Boolean = false) {
    if (!initialized || forceInitialization) {
      AppDB.initialize(
        jdbcUrl = System.getenv("DB_JDBC_URL") ?: sqliteUrl,
        jdbcDriverClassName = System.getenv("DB_JDBC_DRIVER") ?: sqliteDriver,
        username = System.getenv("DB_USERNAME") ?: "",
        password = System.getenv("DB_PASSWORD") ?: "",
        maximumPoolSize = 3
      ) {
        SchemaUtils.create(Products, Stock, Sales, SaleItems)
        initialized = true
      }
    }
  }

  fun resetTables() {
    transaction {
      SchemaUtils.drop(Products, Stock, Sales, SaleItems)
      SchemaUtils.create(Products, Stock, Sales, SaleItems)
    }
  }
}