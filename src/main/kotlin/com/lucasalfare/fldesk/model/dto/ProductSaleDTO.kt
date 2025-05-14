package com.lucasalfare.fldesk.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductSaleDTO(
  val barcode: String,
  val quantity: Int
)