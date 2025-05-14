package com.lucasalfare.fldesk.model.dto

import com.lucasalfare.fldesk.model.PaymentType
import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class SaleRequestDTO(
  val id: Int = -1,
  val paymentType: PaymentType,
  val items: List<ProductSaleDTO>,
  val total: Int = -1,
  val date: Instant = Clock.System.now()
)