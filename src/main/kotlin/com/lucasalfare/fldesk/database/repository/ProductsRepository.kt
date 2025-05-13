package com.lucasalfare.fldesk.database.repository

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.Products
import com.lucasalfare.fldesk.model.Product
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll
import org.jetbrains.exposed.sql.update

object ProductsRepository {

  fun create(name: String, price: Int, barcode: String): Int = runCatching {
    Products.insertAndGetId {
      it[Products.name] = name
      it[Products.price] = price
      it[Products.barcode] = barcode
    }.value
  }.getOrElse {
    throw AppError("Error inserting new product in the database.", parent = it)
  }

  fun getById(id: Int): Product? = runCatching {
    Products.selectAll().where { Products.id eq id }.singleOrNull().let {
      if (it == null) null
      else Product(
        id = it[Products.id].value,
        name = it[Products.name],
        price = it[Products.price],
        barcode = it[Products.barcode]
      )
    }
  }.getOrElse {
    throw AppError("Error on getting product by ID.", parent = it)
  }

  fun getByName(name: String): Product? = runCatching {
    Products.selectAll().where { Products.name eq name }.singleOrNull().let {
      if (it == null) null
      else Product(
        id = it[Products.id].value,
        name = it[Products.name],
        price = it[Products.price],
        barcode = it[Products.barcode]
      )
    }
  }.getOrElse {
    throw AppError("Error on getting product by name.", parent = it)
  }

  fun updatePrice(id: Int, newPrice: Int): Boolean = runCatching {
    Products.update({ Products.id eq id }) {
      it[Products.price] = newPrice
    } > 0
  }.getOrElse {
    throw AppError("Error updating price of product by ID.", parent = it)
  }
}