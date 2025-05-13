package com.lucasalfare.fldesk.model

data class Product(
  val id: Int = -1,
  val name: String,
  val price: Int,
  val barcode: String
)