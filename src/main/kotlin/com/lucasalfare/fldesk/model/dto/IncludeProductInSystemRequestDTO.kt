package com.lucasalfare.fldesk.model.dto

import com.lucasalfare.flbase.AppError
import kotlinx.serialization.Serializable

@Serializable
data class IncludeProductInSystemRequestDTO(
  val name: String,
  val price: Int,
  val barcode: String,
  val quantity: Int = 0
) {

  init {
    if (name.isEmpty()) {
      throw AppError("Invalid product name")
    }

    if (price <= 0) {
      throw AppError("Invalid product price")
    }

    if (barcode.isEmpty()) {
      throw AppError("Invalid product barcode")
    }

    if (quantity < 0) {
      throw AppError("Invalid product stock quantity")
    }
  }
}