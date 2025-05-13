package usecase

import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.dto.IncludeProductInSystemRequestDTO
import com.lucasalfare.fldesk.usecase.ProductsUsecase
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.*

class ProductsUsecasesTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test include product in system success`() {
    val name = "Dummy"
    val price = 500
    val barcode = "00000000001"
    val includingQuantity = 100

    val request = IncludeProductInSystemRequestDTO(name, price, barcode, quantity = includingQuantity)
    val usecases = ProductsUsecase(ProductsRepository, StockRepository, AtomicExecutor)

    val id = runBlocking {
      assertDoesNotThrow {
        usecases.includeProductInSystem(request)
      }
    }

    assertTrue(id == 1)

    runBlocking {
      AtomicExecutor.exec {
        assertEquals(expected = includingQuantity, actual = StockRepository.getQuantityOf(id))
      }
    }
  }
}