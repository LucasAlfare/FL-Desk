package com.lucasalfare.fldesk.usecase

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.IncludeProductInSystemRequestDTO

class ProductsUsecase(
  private val products: ProductsRepository,
  private val stock: StockRepository,
  private val executor: AtomicExecutor
) {

  suspend fun includeProductInSystem(
    request: IncludeProductInSystemRequestDTO
  ): Int = executor.exec {
    runCatching {
      val productId = products.create(name = request.name, price = request.price, barcode = request.barcode)
      stock.insert(productId = productId, quantity = request.quantity)
      productId
    }.getOrElse {
      throw AppError("Error inserting product in the system with the following input request: [$request]")
    }
  }

  suspend fun updateProductQuantityInStock(productId: Int, newQuantity: Int): Boolean = executor.exec {
    runCatching {
      stock.updateQuantity(productId, newQuantity)
    }.getOrElse {
      throw AppError("Error updating product quantity in the stock")
    }
  }
}