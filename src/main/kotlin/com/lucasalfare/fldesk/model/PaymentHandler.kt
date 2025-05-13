package com.lucasalfare.fldesk.model

interface PaymentHandler {

  suspend fun pay(amount: Int, paymentType: PaymentType)
}