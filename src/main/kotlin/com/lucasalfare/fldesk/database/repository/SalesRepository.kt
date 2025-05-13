package com.lucasalfare.fldesk.database.repository

import com.lucasalfare.flbase.AppError
import com.lucasalfare.fldesk.PaymentType
import com.lucasalfare.fldesk.Sale
import com.lucasalfare.fldesk.SoldProduct
import com.lucasalfare.fldesk.database.SaleItems
import com.lucasalfare.fldesk.database.Sales
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toInstant
import kotlinx.datetime.toLocalDateTime
import org.jetbrains.exposed.sql.insert
import org.jetbrains.exposed.sql.insertAndGetId
import org.jetbrains.exposed.sql.selectAll

object SalesRepository {

  fun createSale(
    instant: Instant,
    paymentType: PaymentType,
    items: List<SoldProduct>
  ): Int = runCatching {
    val saleId = Sales.insertAndGetId {
      it[Sales.instant] = instant.toLocalDateTime(TimeZone.UTC)
      it[Sales.total] = items.sumOf { p -> p.priceAtMoment }
      it[Sales.paymentType] = paymentType
    }.value

    items.forEach { sp ->
      SaleItems.insert { it2 ->
        it2[SaleItems.saleId] = saleId
        it2[SaleItems.productId] = sp.productId
        it2[SaleItems.quantitySold] = sp.quantitySold
        it2[SaleItems.priceAtMoment] = sp.priceAtMoment
      }
    }

    saleId
  }.getOrElse {
    throw AppError("Error creating sale registry.")
  }

  fun getById(id: Int): Sale? = runCatching {
    Sales.selectAll().where { Sales.id eq id }.singleOrNull().let {
      if (it == null) null
      else {
        val soldItems = runCatching {
          SaleItems.selectAll().where { SaleItems.saleId eq id }.map { si ->
            SoldProduct(
              productId = si[SaleItems.productId],
              quantitySold = si[SaleItems.quantitySold],
              priceAtMoment = si[SaleItems.priceAtMoment]
            )
          }
        }.getOrElse {
          throw AppError("Error retrieving sold items from SaleItems table.")
        }

        Sale(
          id = it[Sales.id].value,
          instant = it[Sales.instant].toInstant(TimeZone.UTC),
          paymentType = it[Sales.paymentType],
          soldProducts = soldItems
        )
      }
    }
  }.getOrElse {
    throw AppError("Error retrieving Sale registry.")
  }

  fun getByDateRange(start: Instant, end: Instant): List<Sale> = runCatching {
    Sales.selectAll().where {
      Sales.instant.between(
        start.toLocalDateTime(TimeZone.UTC),
        end.toLocalDateTime(TimeZone.UTC)
      )
    }.map {
      val currentSaleId = it[Sales.id].value

      val soldItems = runCatching {
        SaleItems.selectAll().where { SaleItems.saleId eq currentSaleId }.map { si ->
          SoldProduct(
            productId = si[SaleItems.productId],
            quantitySold = si[SaleItems.quantitySold],
            priceAtMoment = si[SaleItems.priceAtMoment]
          )
        }
      }.getOrElse {
        throw AppError("Error retrieving sold items from SaleItems table.")
      }

      Sale(
        id = currentSaleId,
        instant = it[Sales.instant].toInstant(TimeZone.UTC),
        paymentType = it[Sales.paymentType],
        soldProducts = soldItems
      )
    }
  }.getOrElse {
    throw AppError("Error retrieving sales by date range.")
  }

  fun getAll(): List<Sale> = runCatching {
    Sales.selectAll().map {
      val currentSaleId = it[Sales.id].value

      val soldItems = runCatching {
        SaleItems.selectAll().where { SaleItems.saleId eq currentSaleId }.map { si ->
          SoldProduct(
            productId = si[SaleItems.productId],
            quantitySold = si[SaleItems.quantitySold],
            priceAtMoment = si[SaleItems.priceAtMoment]
          )
        }
      }.getOrElse {
        throw AppError("Error retrieving sold items from SaleItems table.")
      }

      Sale(
        id = currentSaleId,
        instant = it[Sales.instant].toInstant(TimeZone.UTC),
        paymentType = it[Sales.paymentType],
        soldProducts = soldItems
      )
    }
  }.getOrElse {
    throw AppError("Error getting all the sales registries.")
  }
}