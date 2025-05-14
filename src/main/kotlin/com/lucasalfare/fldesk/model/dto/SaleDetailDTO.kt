package com.lucasalfare.fldesk.model.dto

import com.lucasalfare.fldesk.model.PaymentType
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SaleDetailDTO(
  val id: Int,
  val date: Instant,
  val paymentType: PaymentType,
  val items: List<ProductSoldDTO>
)