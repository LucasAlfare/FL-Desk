package repository

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.model.Product
import kotlinx.coroutines.runBlocking
import org.junit.jupiter.api.assertDoesNotThrow
import org.junit.jupiter.api.assertThrows
import kotlin.test.*

class ProductsRepositoryTest {

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test create product success`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    runBlocking {
      AtomicExecutor.exec {
        val productId: Int = assertDoesNotThrow {
          ProductsRepository.create(name, price, barcode)
        }
        assertTrue(productId == 1)
      }
    }
  }

  @Test
  fun `test create product failure`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    runBlocking { AtomicExecutor.exec { ProductsRepository.create(name, price, barcode) } }

    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> { ProductsRepository.create(name, price, barcode) }
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> {
          ProductsRepository.create(
            name,
            price,
            barcode
          )
        }
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> {
          ProductsRepository.create(
            "Dummy_2-" + "qiwduhfa87h3o8feh3807hefia3u8hf39a078efha3io7efh3a87oh3o83a7hf8o0a37f4ghe8o3a7gf4h8oa7fg",
            price,
            barcode
          )
        }
      }
    }

    runBlocking {
      AtomicExecutor.exec {
        assertThrows<AppError> {
          ProductsRepository.create(
            "Dummy_3",
            price,
            barcode + "qiwduhfa87h3o8feh3807hefia3u8hf39a078efha3io7efh3a87oh3o83a7hf8o0a37f4ghe8o3a7gf4h8oa7fg9w8dfh90wedhfgv9-0wh78v08w7hv80d7wghv80wd7vh80wehvw8pergvh8"
          )
        }
      }
    }
  }

  @Test
  fun `test get by ID success`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    val productId: Int = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create(name, price, barcode)
      }
    }

    val productFound: Product? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow { ProductsRepository.getById(productId) }
      }
    }

    assertNotNull(productFound)
  }

  @Test
  fun `test get by ID null success`() {
    val productFound: Product? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow { ProductsRepository.getById(0) }
      }
    }

    assertNull(productFound)
  }

  @Test
  fun `test get by name success`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create(name, price, barcode)
      }
    }

    val productFound: Product? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow { ProductsRepository.getByName(name) }
      }
    }

    assertNotNull(productFound)
  }

  @Test
  fun `test get by name null success`() {
    val productFound: Product? = runBlocking {
      AtomicExecutor.exec {
        assertDoesNotThrow { ProductsRepository.getByName("Dummy") }
      }
    }

    assertNull(productFound)
  }

  @Test
  fun `test update price true success`() {
    val name = "Dummy"
    val price = 100
    val barcode = "00000000001"

    val productId = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.create(name, price, barcode)
      }
    }

    val newPrice = 200

    val updateResult = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.updatePrice(productId, newPrice)
      }
    }

    assertTrue(updateResult)

    val searchProduct = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.getById(productId)
      }
    }

    assertEquals(expected = newPrice, actual = searchProduct!!.price)
  }

  @Test
  fun `test update price false success`() {
    val newPrice = 200
    val updateResult = runBlocking {
      AtomicExecutor.exec {
        ProductsRepository.updatePrice(0, newPrice)
      }
    }

    assertFalse(updateResult)
  }


}