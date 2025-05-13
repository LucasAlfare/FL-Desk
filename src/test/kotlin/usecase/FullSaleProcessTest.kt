package usecase

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.Product
import com.lucasalfare.fldesk.model.SoldProduct
import com.lucasalfare.fldesk.model.dto.CommitSaleRequestDTO
import com.lucasalfare.fldesk.model.dto.IncludeProductInSystemRequestDTO
import com.lucasalfare.fldesk.usecase.DummyPaymentHandler
import com.lucasalfare.fldesk.usecase.ProductsUsecase
import com.lucasalfare.fldesk.usecase.SaleUsecases
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class FullSaleProcessTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test full process using dummy payment handler success`() {
    val product = Product(
      name = "Dummy",
      price = 1000,
      barcode = "000111"
    )
    val productsUsecase = ProductsUsecase(
      ProductsRepository,
      StockRepository,
      AtomicExecutor
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
      StockRepository,
      SalesRepository,
      AtomicExecutor
    )

    val products = listOf(SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price))
    val paymentType = PaymentType.Cash

    val saleId = runBlocking {
      salesUsecase.commitSale(
        request = CommitSaleRequestDTO(
          paymentType = paymentType,
          items = products
        )
      ) {
        DummyPaymentHandler.pay(
          amount = products.sumOf { it.priceAtMoment * it.quantitySold },
          paymentType = paymentType
        )
      }
    }

    assertEquals(expected = saleId, actual = 1)
  }

  @Test
  fun `test full process using dummy payment handler failure`() {
    val product = Product(
      name = "Dummy",
      price = 1000,
      barcode = "000111"
    )
    val productsUsecase = ProductsUsecase(
      ProductsRepository,
      StockRepository,
      AtomicExecutor
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
      StockRepository,
      SalesRepository,
      AtomicExecutor
    )

    val products = listOf(SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price))
    val paymentType = PaymentType.Cash

    assertThrows<AppError> {
      runBlocking {
        salesUsecase.commitSale(
          request = CommitSaleRequestDTO(
            paymentType = paymentType,
            items = products
          )
        ) {
          // using specific failable function
          DummyPaymentHandler.failablePay(
            amount = products.sumOf { it.priceAtMoment * it.quantitySold },
            paymentType = paymentType
          )
        }
      }
    }

    val searches = runBlocking { salesUsecase.getAllSales() }

    assertTrue(searches.isEmpty())
  }
}