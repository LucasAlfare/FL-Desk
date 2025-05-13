package com.lucasalfare.fldesk.model

import kotlinx.datetime.Instant
import kotlinx.serialization.Serializable

@Serializable
data class Sale(
  val id: Int,
  val instant: Instant,
  val paymentType: PaymentType,
  val soldProducts: List<SoldProduct>
)