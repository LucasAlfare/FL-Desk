package com.lucasalfare.fldesk.model

import kotlinx.serialization.Serializable

@Serializable
data class SoldProduct(
  val productId: Int,
  val quantitySold: Int,
  val priceAtMoment: Int
)