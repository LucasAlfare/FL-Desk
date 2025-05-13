package com.lucasalfare.fldesk

import com.lucasalfare.flbase.*
import com.lucasalfare.fldesk.database.AtomicExecutor
import com.lucasalfare.fldesk.database.ExposedDatabase
import com.lucasalfare.fldesk.database.repository.ProductsRepository
import com.lucasalfare.fldesk.database.repository.SalesRepository
import com.lucasalfare.fldesk.database.repository.StockRepository
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.dto.CommitSaleRequestDTO
import com.lucasalfare.fldesk.model.dto.IncludeProductInSystemRequestDTO
import com.lucasalfare.fldesk.usecase.DummyPaymentHandler
import com.lucasalfare.fldesk.usecase.ProductsUsecase
import com.lucasalfare.fldesk.usecase.SaleUsecases
import io.ktor.http.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlin.random.Random

suspend fun main() {
  val productsUsecase = ProductsUsecase(
    ProductsRepository,
    StockRepository,
    AtomicExecutor
  )

  val saleUsecases = SaleUsecases(
    StockRepository,
    SalesRepository,
    AtomicExecutor
  )

  ExposedDatabase.initialize()

  startWebServer(port = 3000) {
    configureCORS()
    configureSerialization()
    configureStatusPages()
    configureRouting {
      get("/health") {
        call.respondText { "Hello from KTOR!" }
      }

      post("/products") {
        val request = call.receive<IncludeProductInSystemRequestDTO>()
        val resultId = productsUsecase.includeProductInSystem(request)
        return@post call.respond(HttpStatusCode.Created, resultId)
      }

      get("/sales") {
        call.respond(HttpStatusCode.OK, saleUsecases.getAllSales())
      }

      post("/sales") {
        val request = call.receive<CommitSaleRequestDTO>()
        val saleId = saleUsecases.commitSale(request) {
          if (Random.nextBoolean()) {
            DummyPaymentHandler.pay(
              amount = request.items.sumOf { it.priceAtMoment * it.quantitySold },
              paymentType = PaymentType.Cash
            )
          } else {
            DummyPaymentHandler.failablePay(
              amount = request.items.sumOf { it.priceAtMoment * it.quantitySold },
              paymentType = PaymentType.Cash
            )
          }
        }
        return@post call.respond(HttpStatusCode.Created, saleId)
      }
    }
  }
}