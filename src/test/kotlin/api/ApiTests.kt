package api

import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.setupServer
import io.ktor.client.plugins.contentnegotiation.*
import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.HttpStatusCode
import io.ktor.serialization.kotlinx.json.*
import io.ktor.server.testing.*
import kotlinx.coroutines.test.runTest
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
  fun ok() = runTest {
    val response = client.get("/health")
    assertEquals(expected = HttpStatusCode.OK, response.status)
  }
}