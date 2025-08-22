package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.CurrencyEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.github.ai.split.utils.some
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

class CurrencyEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[CurrencyEntity]] = {
    val query = quote {
      querySchema[CurrencyEntity]("currencies")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def findByIsoCode(isoCode: String): IO[DomainError, Option[CurrencyEntity]] = {
    val query = quote {
      querySchema[CurrencyEntity]("currencies")
        .filter(_.isoCode == lift(isoCode))
    }

    for {
      currencies <- run(query).mapError(_.toDomainError())
    } yield currencies.headOption
  }

  def getByIsoCode(isoCode: String): IO[DomainError, CurrencyEntity] = {
    val query = quote {
      querySchema[CurrencyEntity]("currencies")
        .filter(_.isoCode == lift(isoCode))
    }

    for {
      currencies <- run(query).mapError(_.toDomainError())
      currency <-
        if (currencies.nonEmpty) {
          ZIO.succeed(currencies.head)
        } else {
          ZIO.fail(DomainError(message = s"Failed to find currency by ISO code: $isoCode".some))
        }
    } yield currency
  }

  def getByIsoCodes(isoCodes: List[String]): IO[DomainError, List[CurrencyEntity]] = {
    val isoCodeSet = isoCodes.toSet

    val query = quote {
      querySchema[CurrencyEntity]("currencies")
        .filter(currency => liftQuery(isoCodeSet).contains(currency.isoCode))
    }

    for {
      currencies <- run(query).mapError(_.toDomainError())
      _ <-
        if (currencies.size != isoCodes.size) {
          val foundIsoCodes = currencies.map(_.isoCode).toSet
          val notFoundIsoCodes = isoCodes.filterNot(foundIsoCodes.contains).mkString(", ")
          ZIO.fail(DomainError(message = s"Failed to find currencies: $notFoundIsoCodes".some))
        } else {
          ZIO.succeed(())
        }
    } yield currencies
  }

  def add(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] = {
    run(
      quote {
        querySchema[CurrencyEntity]("currencies")
          .insertValue(lift(currency))
      }
    )
      .map(_ => currency)
      .mapError(_.toDomainError())
  }

  def addBatch(currencies: List[CurrencyEntity]): IO[DomainError, List[CurrencyEntity]] = {
    run(
      quote {
        liftQuery(currencies).foreach(currency =>
          querySchema[CurrencyEntity]("currencies")
            .insertValue(currency)
        )
      }
    )
      .map(_ => currencies)
      .mapError(_.toDomainError())
  }

  def update(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] = {
    val updateQuery = quote {
      querySchema[CurrencyEntity]("currencies")
        .filter(_.isoCode == lift(currency.isoCode))
        .updateValue(lift(currency))
    }

    run(updateQuery)
      .map(_ => currency)
      .mapError(_.toDomainError())
  }
}
