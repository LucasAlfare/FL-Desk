package com.lucasalfare.fldesk

import com.lucasalfare.flbase.*
import com.lucasalfare.fldesk.database.*
import com.lucasalfare.fldesk.model.PaymentHandler
import com.lucasalfare.fldesk.model.PaymentType
import com.lucasalfare.fldesk.model.dto.ProductDTO
import com.lucasalfare.fldesk.model.dto.ProductSoldDTO
import com.lucasalfare.fldesk.model.dto.SaleDetailDTO
import com.lucasalfare.fldesk.model.dto.SaleRequestDTO
import io.ktor.http.*
import io.ktor.server.application.*
import io.ktor.server.request.*
import io.ktor.server.response.*
import io.ktor.server.routing.*
import kotlinx.coroutines.delay
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.*
import kotlin.random.Random

// just dummy!
val paymentHandler = object : PaymentHandler {
  override suspend fun pay(amount: Int, paymentType: PaymentType) {
    delay(2000L)
    if (Random.nextBoolean()) {
      println("successful paid!")
    } else {
      throw AppError(status = HttpStatusCode.UnprocessableEntity, customMessage = "Payment was not processed!")
    }
  }
}

suspend fun main() {
  ExposedDatabase.initialize()

  // create fake products
  runCatching {
    exec {
      Products.insertAndGetId {
        it[Products.barcode] = "0001"
        it[Products.name] = "Produto 1"
        it[Products.price] = 500
      }.value.also { id ->
        Stock.insert {
          it[Stock.productId] = id
          it[Stock.quantity] = 20
        }
      }

      Products.insertAndGetId {
        it[Products.barcode] = "0002"
        it[Products.name] = "Produto 2 fake"
        it[Products.price] = 750
      }.value.also { id ->
        Stock.insert {
          it[Stock.productId] = id
          it[Stock.quantity] = 50
        }
      }
    }
  }

  startWebServer(port = 3000) {
    setupServer()
  }
}

fun Application.setupServer() {
  configureCORS()
  configureSerialization()
  configureStatusPages()
  configureStaticHtml(Pair("/", "index.html"))
  configureRouting {
    get("/health") {
      call.respondText { "Hello from KTOR!" }
    }

    // curl http://localhost:3000/products/barcode/1234567890123
    get("/products/barcode/{barcode}") {
      val barcode = call.pathParameters["barcode"] ?: throw AppError("Bad request")
      return@get exec {
        val searchProduct: ResultRow? = Products.selectAll().where { Products.barcode eq barcode }.singleOrNull()
        if (searchProduct == null) throw AppError(
          status = HttpStatusCode.NotFound, customMessage = "Product not found"
        )

        val quantityInStock = Stock.selectAll().where { Stock.productId eq searchProduct[Products.id].value }.single()

        if (quantityInStock[Stock.quantity] <= 0) throw AppError(
          status = HttpStatusCode.NotFound, customMessage = "Product not enough to sell"
        )

        val p = ProductDTO(
          id = searchProduct[Products.id].value,
          barcode = searchProduct[Products.barcode],
          name = searchProduct[Products.name],
          price = searchProduct[Products.price],
          quantity = quantityInStock[Stock.quantity]
        )

        call.respond(HttpStatusCode.OK, p)
      }
    }

    // curl -X POST http://localhost:3000/products -H "Content-Type: application/json" -d '{\"barcode\":\"1234567890123\",\"name\":\"Produto Teste\",\"price\":10,\"quantity\":100}'
    post("/products") {
      val request = call.receive<ProductDTO>()
      val result = runCatching {
        exec {
          Products.insertAndGetId {
            it[Products.name] = request.name
            it[Products.barcode] = request.barcode
            it[Products.price] = request.price
          }.value.also { id ->
            Stock.insert {
              it[Stock.productId] = id
              it[Stock.quantity] = request.quantity
            }
          }
        }
      }.getOrElse {
        if (it is AppError) {
          throw it
        } else {
          throw AppError(
            status = HttpStatusCode.UnprocessableEntity,
            customMessage = "Error creating product in the database",
            parent = it
          )
        }
      }
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // curl -X POST http://localhost:3000/sales -H "Content-Type: application/json" -d '{\"paymentType\":\"Pix\",\"date\":\"2025-05-14T15:00:00Z\",\"items\":[{\"barcode\":\"1234567890123\",\"quantity\":2}]}'
    post("/sales") {
      /*
      we receive simple data from client: payment type and items. The
      items have the data: barcode and quantity. Using this data we
      convert it to a list of the [ProductDTO] type.
       */
      val request = call.receive<SaleRequestDTO>()
      val result = runCatching {
        exec {
          val products = request.items.map { req ->
            Products.selectAll().where { Products.barcode eq req.barcode }.singleOrNull().let { pq ->
              if (pq == null) {
                throw AppError(
                  status = HttpStatusCode.UnprocessableEntity,
                  customMessage = "Item of barcode [${req.barcode}] not found"
                )
              }

              val searchedId = pq[Products.id].value

              val searchedQuantity = Stock.selectAll().where { Stock.productId eq searchedId }.single().let { sq ->
                sq[Stock.quantity]
              }

              // we also ensure the quantity is enough to sell
              if (req.quantity > searchedQuantity) {
                throw AppError(
                  status = HttpStatusCode.UnprocessableEntity,
                  customMessage = "Item of barcode [${req.barcode}] not enough to sell."
                )
              }

              Stock.update(where = { Stock.productId eq searchedId }) {
                it[Stock.quantity] = searchedQuantity - req.quantity
              }

              ProductDTO(
                id = searchedId,
                barcode = pq[Products.barcode],
                name = pq[Products.name],
                price = pq[Products.price],
                quantity = searchedQuantity
              )
            }
          }

          val total = products.sumOf { p ->
            p.quantity * p.price
          }

          val saleId = Sales.insertAndGetId {
            it[Sales.instant] = request.date.toLocalDateTime(TimeZone.UTC)
            it[Sales.total] = total
            it[Sales.paymentType] = request.paymentType
          }.value

          products.forEach { p ->
            SaleItems.insert {
              it[SaleItems.saleId] = saleId
              it[SaleItems.productId] = p.id
              it[SaleItems.quantitySold] = p.quantity
              it[SaleItems.priceAtMoment] = p.price
            }
          }

          runCatching {
            // if fails, the transaction will just rollbacks
            paymentHandler.pay(amount = total, request.paymentType)
          }.onFailure { t ->
            if (t is AppError) {
              throw t
            } else {
              throw AppError(
                status = HttpStatusCode.UnprocessableEntity,
                customMessage = "Payment not accepted!",
                parent = t
              )
            }
          }

          saleId
        }
      }.getOrElse {
        if (it is AppError) {
          throw it
        } else {
          throw AppError(customMessage = "Error creating sale.", parent = it)
        }
      }
      return@post call.respond(HttpStatusCode.Created, result)
    }

    // curl http://localhost:3000/sales
    get("/sales") {
      val result = runCatching {
        exec {
          Sales.selectAll().map { sq ->
            val saleId = sq[Sales.id].value

            val soldItems = SaleItems.selectAll().where { SaleItems.saleId eq saleId }.map { siq ->
              ProductSoldDTO(
                productId = siq[SaleItems.productId],
                quantitySold = siq[SaleItems.quantitySold],
                priceAtMoment = siq[SaleItems.priceAtMoment]
              )
            }

            return@map SaleDetailDTO(
              id = saleId,
              date = sq[Sales.instant].toInstant(TimeZone.UTC),
              paymentType = sq[Sales.paymentType],
              items = soldItems
            )
          }
        }
      }.getOrElse {
        if (it is AppError) {
          throw it
        } else {
          throw AppError(customMessage = "Error retrieving all sales.", parent = it)
        }
      }

      return@get call.respond(HttpStatusCode.OK, result)
    }
  }
}