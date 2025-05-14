package api

import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.dto.ProductDTO
import com.lucasalfare.fldesk.model.dto.ProductSaleDTO
import com.lucasalfare.fldesk.model.dto.SaleRequestDTO
import com.lucasalfare.fldesk.setupServer
import io.ktor.client.call.*
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.http.*
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
import org.junit.jupiter.api.assertDoesNotThrow
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

class ApiTests {

  val app = TestApplication {
    application {
      setupServer()
    }
  }

  val client = app.createClient {
    install(ContentNegotiation) { json() }
  }

  @BeforeTest
  fun setup() {
    ExposedDatabase.initialize()
  }

  @AfterTest
  fun dispose() {
    ExposedDatabase.resetTables()
  }

  @Test
  fun `test health route success`() = runTest {
    val response = client.get("/health")
    assertEquals(expected = HttpStatusCode.OK, response.status)
  }

  @Test
  fun `test POST -products success`() = runTest {
    val barcode = "123456789"
    val name = "Dummy"
    val price = 100
    val quantity = 10

    val response = client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = quantity
        )
      )
    }

    assertEquals(expected = HttpStatusCode.Created, response.status)
  }

  @Test
  fun `test POST -products failure`() = runTest {
    val barcode = "123456789"
    val name = "Dummy"
    val price = 100
    val quantity = 10

    client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = quantity
        )
      )
    }

    val response = client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = quantity
        )
      )
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, response.status)
  }

  @Test
  fun `test GET -products-barcode-{barcode} success`() = runTest {
    val barcode = "123456789"
    val name = "Dummy"
    val price = 100
    val quantity = 10

    client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = quantity
        )
      )
    }

    val response = client.get("/products/barcode/$barcode")
    assertEquals(expected = HttpStatusCode.OK, actual = response.status)
    val product = assertDoesNotThrow { response.body<ProductDTO>() }
    assertEquals(expected = barcode, actual = product.barcode)
    assertEquals(expected = name, actual = product.name)
    assertEquals(expected = price, actual = product.price)
    assertEquals(expected = quantity, actual = product.quantity)
  }

  @Test
  fun `test GET -products-barcode-{barcode} failure`() = runTest {
    val barcode = "123456789"
    val response = client.get("/products/barcode/$barcode")
    assertEquals(expected = HttpStatusCode.NotFound, actual = response.status)
  }

  @Test
  fun `test POST -sales success`() = runTest {
    val barcode = "123456789"
    val name = "Dummy"
    val price = 100
    val stockQuantity = 10

    client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = stockQuantity
        )
      )
    }

    val buyingQuantity = 5
    val paymentType = PaymentType.Pix
    val items = listOf(
      ProductSaleDTO(
        barcode = barcode,
        quantity = buyingQuantity
      )
    )

    val result = client.post("/sales") {
      contentType(ContentType.Application.Json)
      setBody(
        SaleRequestDTO(
          paymentType = paymentType,
          items = items
        )
      )
    }

    assertEquals(expected = HttpStatusCode.Created, actual = result.status)
  }

  @Test
  fun `test POST -sales failure by unknown product`() = runTest {
    val buyingQuantity = 5
    val paymentType = PaymentType.Pix
    val items = listOf(
      ProductSaleDTO(
        barcode = "123456789",
        quantity = buyingQuantity
      )
    )

    val result = client.post("/sales") {
      contentType(ContentType.Application.Json)
      setBody(SaleRequestDTO(paymentType = paymentType, items = items))
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, actual = result.status)
  }

  @Test
  fun `test POST -sales failure by low stock`() = runTest {
    val barcode = "123456789"
    val name = "Dummy"
    val price = 100
    val stockQuantity = 10

    client.post("/products") {
      contentType(ContentType.Application.Json)
      setBody(
        ProductDTO(
          barcode = barcode,
          name = name,
          price = price,
          quantity = stockQuantity
        )
      )
    }

    val buyingQuantity = 20
    val paymentType = PaymentType.Pix
    val items = listOf(
      ProductSaleDTO(
        barcode = barcode,
        quantity = buyingQuantity
      )
    )

    val result = client.post("/sales") {
      contentType(ContentType.Application.Json)
      setBody(
        SaleRequestDTO(
          paymentType = paymentType,
          items = items
        )
      )
    }

    assertEquals(expected = HttpStatusCode.UnprocessableEntity, actual = result.status)
  }
}