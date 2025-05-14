package com.lucasalfare.fldesk.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductSoldDTO(
  val productId: Int,
  val quantitySold: Int,
  val priceAtMoment: Int
)