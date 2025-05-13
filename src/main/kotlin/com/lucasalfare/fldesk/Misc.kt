package com.lucasalfare.fldesk

import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.SoldProduct
import com.lucasalfare.fldesk.model.dto.CommitSaleRequestDTO
import com.lucasalfare.fldesk.model.dto.IncludeProductInSystemRequestDTO
import kotlin.random.Random

fun generateValidProduct(): IncludeProductInSystemRequestDTO {
  val names = listOf("Banana", "Notebook", "Caneta", "Shampoo", "Chocolate")
  val name = names.random()

  val price = Random.nextInt(1, 10_000)

  val barcode = List(13) { Random.nextInt(0, 10) }.joinToString("") // EAN-13 format

  val quantity = Random.nextInt(0, 100)

  return IncludeProductInSystemRequestDTO(
    name = name,
    price = price,
    barcode = barcode,
    quantity = quantity
  )
}

fun generateValidCommitSale(): CommitSaleRequestDTO {
  val paymentType = PaymentType.entries.random()

  val items = List(Random.nextInt(1, 5)) {
    SoldProduct(
      productId = Random.nextInt(1, 1000),
      quantitySold = Random.nextInt(1, 10),
      priceAtMoment = Random.nextInt(100, 10_000) // preço em centavos
    )
  }

  return CommitSaleRequestDTO(
    paymentType = paymentType,
    items = items
  )
}