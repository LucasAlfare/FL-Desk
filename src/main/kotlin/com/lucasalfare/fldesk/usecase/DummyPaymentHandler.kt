package com.lucasalfare.fldesk.usecase

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.model.PaymentHandler
import com.lucasalfare.fldesk.model.PaymentType
import kotlinx.coroutines.delay

object DummyPaymentHandler : PaymentHandler {
  override suspend fun pay(amount: Int, paymentType: PaymentType) {
    println("Trying to pay [$amount] using type [$paymentType]...")
    delay(1000L)
    println("Successful paid [$amount] using type [$paymentType]!")
  }

  suspend fun failablePay(amount: Int, paymentType: PaymentType) {
    println("Trying to pay [$amount] using type [$paymentType]...")
    delay(1000L)
    throw AppError("Unable to pay [$amount] using type [$paymentType]! :(")
  }
}