package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.entity.db.CurrencyEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}
import slick.jdbc.PostgresProfile.api.*

class CurrencyEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.CurrencyTable) {

  private val table = db.CurrencyTable

  def getAll(): IO[DomainError, List[CurrencyEntity]] = {
    queryAll()
  }

  def findByIsoCode(isoCode: String): IO[DomainError, Option[CurrencyEntity]] = {
    queryOne(t => t.isoCode === isoCode)
  }

  def getByIsoCode(isoCode: String): IO[DomainError, CurrencyEntity] = {
    queryOne(t => t.isoCode === isoCode)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find currency by ISO code: $isoCode".some))
      }
  }

  def getByIsoCodes(isoCodes: List[String]): IO[DomainError, List[CurrencyEntity]] = {
    val isoCodeSet = isoCodes.toSet

    query(t => t.isoCode inSet isoCodeSet)
      .flatMap { currencies =>
        if (currencies.size == isoCodeSet.size) {
          ZIO.succeed(currencies.toList)
        } else {
          val foundIsoCodes = currencies.map(_.isoCode).toSet
          val notFoundIsoCodes = isoCodeSet.diff(foundIsoCodes).mkString(", ")
          ZIO.fail(DomainError(message = s"Failed to find currencies: $notFoundIsoCodes".some))
        }
      }
  }

  def add(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] = {
    insert(currency)
  }

  def update(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] = {
    updateOne(
      predicate = { entity => entity.isoCode === currency.isoCode },
      entity = currency
    )
  }
}
