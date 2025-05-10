package com.lucasalfare.fldesk

import kotlinx.datetime.Instant

data class Product(
  val id: Int,
  val name: String,
  val price: Int,
  val barcode: String
)

data class SoldProduct(
  val productId: Int,
  val quantitySold: Int,
  val priceAtMoment: Int
)

data class Sale(
  val id: Int,
  val instant: Instant,
  val paymentType: PaymentType,
  val soldProducts: List<SoldProduct>
)