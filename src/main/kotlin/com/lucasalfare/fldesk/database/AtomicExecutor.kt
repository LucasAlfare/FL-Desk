package com.lucasalfare.fldesk.database

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AtomicExecutor {

  suspend fun <R> exec(scope: () -> R): R = newSuspendedTransaction {
    scope()
  }
}