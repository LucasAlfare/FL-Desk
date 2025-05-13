package com.lucasalfare.fldesk

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

data class Product(
  val id: Int = -1,
  val name: String,
  val price: Int,
  val barcode: String
)

@Serializable
data class SoldProduct(
  val productId: Int,
  val quantitySold: Int,
  val priceAtMoment: Int
)

@Serializable
data class Sale(
  val id: Int,
  val instant: Instant,
  val paymentType: PaymentType,
  val soldProducts: List<SoldProduct>
)