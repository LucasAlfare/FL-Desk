package com.lucasalfare.fldesk.model.dto

import kotlinx.serialization.Serializable

@Serializable
data class ProductDTO(
  val id: Int = -1,
  val barcode: String,
  val name: String,
  val price: Int,
  val quantity: Int
)