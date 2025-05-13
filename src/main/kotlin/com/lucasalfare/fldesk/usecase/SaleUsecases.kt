package com.lucasalfare.fldesk.usecase

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.Sale
import com.lucasalfare.fldesk.model.SoldProduct
import com.lucasalfare.fldesk.model.dto.CommitSaleRequestDTO
import kotlinx.datetime.Clock

class SaleUsecases(
  private val stock: StockRepository,
  private val sales: SalesRepository,
  private val executor: AtomicExecutor
) {

  suspend fun commitSale(
    request: CommitSaleRequestDTO,
    onSaleCommited: suspend () -> Unit = {}
  ): Int = runCatching {
    executor.exec {
      val instant = Clock.System.now()
      val soldItems = request.items.map { item ->
        val currentQuantity = stock.getQuantityOf(item.productId)
        if (item.quantitySold > currentQuantity) {
          throw AppError("No enough item [${item.productId}] to sell.")
        }

        stock.updateQuantity(item.productId, currentQuantity - item.quantitySold)

        SoldProduct(item.productId, item.quantitySold, item.priceAtMoment)
      }

      val saleId = sales.createSale(instant, request.paymentType, soldItems)

      onSaleCommited() // calls the post callback after the process

      saleId
    }
  }.getOrElse {
    throw AppError("Error commiting the sale.", parent = it)
  }

  suspend fun getAllSales(): List<Sale> = runCatching {
    executor.exec {
      sales.getAll()
    }
  }.getOrElse {
    throw AppError("error getting all sales", parent = it)
  }
}