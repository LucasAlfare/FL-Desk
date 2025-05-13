package repository

import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.Product
import com.lucasalfare.fldesk.model.Sale
import com.lucasalfare.fldesk.model.SoldProduct
import com.lucasalfare.fldesk.model.dto.IncludeProductInSystemRequestDTO
import com.lucasalfare.fldesk.usecase.ProductsUsecase
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*
import kotlin.time.Duration.Companion.days

class SaleRepositoryTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test create sale success`() {
    val saleId = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          SalesRepository.createSale(
            instant = Clock.System.now(),
            paymentType = PaymentType.Cash,
            items = listOf()
          )
        }
      }
    }

    assertTrue(saleId == 1)
  }

  @Test
  fun `test get sale by ID success`() {
    val saleId = runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          instant = Clock.System.now(),
          paymentType = PaymentType.Cash,
          items = listOf()
        )
      }
    }

    val search: Sale? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          SalesRepository.getById(saleId)
        }
      }
    }

    assertNotNull(search)
  }

  @Test
  fun `test get sale by ID failure`() {
    val search: Sale? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          SalesRepository.getById(1)
        }
      }
    }

    assertNull(search)
  }

  @Test
  fun `test get all success`() {
    runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          instant = Clock.System.now(),
          paymentType = PaymentType.Cash,
          items = listOf()
        )
      }
    }

    val all: List<Sale> = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          SalesRepository.getAll()
        }
      }
    }

    assertTrue(all.isNotEmpty())
    assertEquals(expected = 1, actual = all.size)
  }

  @Test
  fun `test get sales by time range success`() {
    // prepare
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

    val now = Clock.System.now()
    val earlier = now - 3.days
    val later = now + 3.days

    runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          earlier,
          PaymentType.Cash,
          listOf(
            SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price)
          )
        )
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          earlier,
          PaymentType.Cash,
          listOf(
            SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price)
          )
        )
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          now,
          PaymentType.Cash,
          listOf(
            SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price)
          )
        )
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        SalesRepository.createSale(
          later,
          PaymentType.Cash,
          listOf(
            SoldProduct(productId = productId, quantitySold = 1, priceAtMoment = product.price)
          )
        )
      }
    }

    // perform
    val searches = runBlocking {
      AtomicExecutor.exec {
        SalesRepository.getByDateRange(
          start = earlier,
          end = later
        )
      }
    }

    // check
    assertEquals(expected = 4, actual = searches.size)
  }
}