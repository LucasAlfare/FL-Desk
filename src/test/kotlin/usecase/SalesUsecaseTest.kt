package usecase

import com.lucasalfare.fldesk.PaymentType
import com.lucasalfare.fldesk.Product
import com.lucasalfare.fldesk.SoldProduct
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.CommitSaleRequestDTO
import com.lucasalfare.fldesk.model.IncludeProductInSystemRequestDTO
import com.lucasalfare.fldesk.usecase.ProductsUsecase
import com.lucasalfare.fldesk.usecase.SaleUsecases
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertTrue

class SalesUsecaseTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test commit sale use case success`() {
    val productsUsecase = ProductsUsecase(
      ProductsRepository,
      StockRepository,
      AtomicExecutor
    )

    val product = Product(
      name = "Dummy",
      price = 1000,
      barcode = "000111"
    )

    val productId = runBlocking {
      productsUsecase.includeProductInSystem(
        IncludeProductInSystemRequestDTO(
          name = product.name,
          price = product.price,
          barcode = product.barcode,
          quantity = 10
        )
      )
    }

    val salesUsecase = SaleUsecases(
      ProductsRepository,
      StockRepository,
      SalesRepository,
      AtomicExecutor
    )

    val saleId = runBlocking {
      assertDoesNotThrow {
        salesUsecase.commitSale(
          CommitSaleRequestDTO(
            PaymentType.Cash,
            listOf(
              SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price)
            )
          )
        )
      }
    }

    assertTrue(saleId == 1)
  }
}