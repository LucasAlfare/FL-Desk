package com.lucasalfare.fldesk.model.dto

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.SoldProduct
import kotlinx.serialization.Serializable

@Serializable
data class CommitSaleRequestDTO(
  val paymentType: PaymentType,
  val items: List<SoldProduct>
) {
  init {
    if (items.isEmpty()) throw AppError("Invalid commit sale request: empty items.")
  }
}