package com.lucasalfare.fldesk.database

import com.lucasalfare.fldesk.PaymentType
import org.jetbrains.exposed.dao.id.IntIdTable
import org.jetbrains.exposed.sql.kotlin.datetime.datetime

object Products : IntIdTable("Products") {
  val name = varchar("name", 20).uniqueIndex()
  val price = integer("price")
  val barcode = varchar("barcode", 50)
}

object Stock : IntIdTable("Stock") {
  val productId = integer("product_id").references(Products.id)
  val quantity = integer("quantity")
}

object Sales : IntIdTable("Sales") {
  val instant = datetime("instant")
  val total = integer("total")
  val paymentType = enumeration<PaymentType>("payment_type")
}

  object SaleItems : IntIdTable("SaleItems") {
  val saleId = integer("sale_id").references(Sales.id)
  val productId = integer("product_id").references(Products.id)
  val quantitySold = integer("quantity_sold")
  val priceAtMoment = integer("price_at_moment")
}