package com.lucasalfare.fldesk.database.repository

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.Stock
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object StockRepository {

  fun insert(productId: Int, quantity: Int): Int = runCatching {
    Stock.insertAndGetId {
      it[Stock.productId] = productId
      it[Stock.quantity] = quantity
    }.value
  }.getOrElse {
    throw AppError("Error inserting product in stock.")
  }

  fun getQuantityOf(productId: Int): Int = runCatching {
    Stock.selectAll().where { Stock.productId eq productId }.singleOrNull().let {
      if (it == null) -1 // no registry found here
      else it[Stock.quantity]
    }
  }.getOrElse {
    throw AppError("Error getting stock product quantity by product ID.")
  }

  fun updateQuantity(productId: Int, newQuantity: Int): Boolean = runCatching {
    Stock.update({ Stock.productId eq productId }) {
      it[Stock.quantity] = newQuantity
    } > 0
  }.getOrElse {
    throw AppError("Error updating stock product quantity by product ID.")
  }
}