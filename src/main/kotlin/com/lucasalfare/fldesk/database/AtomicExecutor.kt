package com.lucasalfare.fldesk.database

import kotlinx.coroutines.Dispatchers
import org.jetbrains.exposed.sql.transactions.experimental.newSuspendedTransaction

suspend fun <R> exec(scope: suspend () -> R): R =
  newSuspendedTransaction(context = Dispatchers.IO) {
    scope()
  }