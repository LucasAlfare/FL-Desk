package com.lucasalfare.fldesk.model

import kotlinx.serialization.Serializable

@Serializable
enum class PaymentType {
  Cash, Credit, Debit, Pix
}