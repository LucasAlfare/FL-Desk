package repository

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class StockRepositoryTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test create stock registry success`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    val productId = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create(name, price, barcode)
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          StockRepository.insert(productId, 10)
        }
      }
    }
  }

  @Test
  fun `test create stock registry failure`() {
    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> {
          StockRepository.insert(0, 10)
        }
      }
    }
  }

  @Test
  fun `test get quantity of product success`() {
    val productId = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create("Dummy", 100, "00000000001")
      }
    }

    val initialQuantity = 10

    runBlocking {
      AtomicExecutor.exec {
        StockRepository.insert(productId, initialQuantity)
      }
    }

    val quantity = runBlocking {
      AtomicExecutor.exec {
        StockRepository.getQuantityOf(productId = productId)
      }
    }

    assertEquals(expected = initialQuantity, actual = quantity)
  }

  @Test
  fun `test get quantity of product failure`() {
    val initialQuantity = 10

    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> {
          StockRepository.insert(0, initialQuantity)
        }
      }
    }
  }

  @Test
  fun `test update stock quantity true success`() {
    val productId = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create("Dummy", 100, "00000000001")
      }
    }

    val initialQuantity = 10

    runBlocking {
      AtomicExecutor.exec {
        StockRepository.insert(productId, initialQuantity)
      }
    }

    val updateResult = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          StockRepository.updateQuantity(productId, initialQuantity * 2)
        }
      }
    }

    assertTrue(updateResult)
  }

  @Test
  fun `test update stock quantity false success`() {
    val updateResult = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow {
          StockRepository.updateQuantity(0, 1)
        }
      }
    }

    assertFalse(updateResult)
  }
}