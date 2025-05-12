package repository

import com.lucasalfare.fldesk.PaymentType
import com.lucasalfare.fldesk.Sale
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.SalesRepository
import kotlinx.coroutines.runBlocking
import kotlinx.datetime.Clock
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

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
            total = 10,
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
          total = 10,
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
          total = 10,
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
}