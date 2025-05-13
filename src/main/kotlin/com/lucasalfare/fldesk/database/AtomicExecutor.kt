package com.lucasalfare.fldesk.database

import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

object AtomicExecutor {

  suspend fun <R> exec(scope: suspend () -> R): R = newSuspendedTransaction {
    scope()
  }
}