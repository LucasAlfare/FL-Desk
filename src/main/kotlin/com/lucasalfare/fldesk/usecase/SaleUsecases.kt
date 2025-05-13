package com.lucasalfare.fldesk.usecase

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.Sale
import com.lucasalfare.fldesk.SoldProduct
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.CommitSaleRequestDTO
import kotlinx.datetime.Clock

class SaleUsecases(
  private val products: ProductsRepository,
  private val stock: StockRepository,
  private val sales: SalesRepository,
  private val executor: AtomicExecutor
) {

  suspend fun commitSale(request: CommitSaleRequestDTO): Int = executor.exec {
    runCatching {
      val instant = Clock.System.now()
      val soldItems = request.items.map { item ->
        val currentQuantity = stock.getQuantityOf(item.productId)
        if (item.quantitySold > currentQuantity) {
          throw AppError("No enough item [${item.productId}] to sell.")
        }

        stock.updateQuantity(item.productId, currentQuantity - item.quantitySold)

        SoldProduct(item.productId, item.quantitySold, item.priceAtMoment)
      }

      sales.createSale(instant, request.paymentType, soldItems)
    }.getOrElse {
      throw AppError("Error commiting the sale.")
    }
  }

  suspend fun getAllSales(): List<Sale> = executor.exec {
    runCatching {
      sales.getAll()
    }.getOrElse {
      throw AppError("error getting all sales")
    }
  }
}