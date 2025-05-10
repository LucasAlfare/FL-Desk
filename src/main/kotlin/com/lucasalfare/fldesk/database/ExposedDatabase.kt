package com.lucasalfare.fldesk.database

import com.lucasalfare.flbase.database.AppDB
import org.jetbrains.exposed.sql.SchemaUtils
import org.jetbrains.exposed.sql.transactions.transaction

object ExposedDatabase {

  private var initialized = false

  fun initialize(forceInitialization: Boolean = false) {
    if (!initialized || forceInitialization) {
      AppDB.initialize(
        jdbcUrl = "jdbc:h2:mem:regular",
        jdbcDriverClassName = "org.h2.Driver",
        username = "",
        password = "",
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