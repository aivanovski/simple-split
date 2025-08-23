package com.github.ai.split.data.currency

import com.github.ai.split.entity.db.CurrencyEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{Resources, parseJson, some}
import zio.*
import zio.direct.*
import zio.json.{DeriveJsonDecoder, JsonDecoder}

class CurrencyParser {

  def parse(): IO[DomainError, List[CurrencyEntity]] = {
    defer {
      val jsonContent = Resources.readResourceAsString("/currencies.json").run
      val items = jsonContent.parseJson[List[CurrencyInfo]].run

      items.flatMap { item =>
        val isoCode = item.currencies.keys.headOption
        val nameAndSymbol = isoCode.flatMap { code => item.currencies.get(code) }

        if (isoCode.isDefined
          && nameAndSymbol.isDefined
          && !isoCode.get.isBlank
          && !nameAndSymbol.get.name.isBlank
          && !nameAndSymbol.get.symbol.isBlank) {

          CurrencyEntity(
            name = nameAndSymbol.get.name.trim,
            isoCode = isoCode.get.trim,
            symbol = nameAndSymbol.get.symbol.trim
          ).some
        } else {
          None
        }
      }
    }
  }

  case class CurrencyItem(
    name: String,
    symbol: String
  )

  case class CurrencyInfo(
    currencies: Map[String, CurrencyItem]
  )

  implicit val infoDecoder: JsonDecoder[CurrencyInfo] = DeriveJsonDecoder.gen[CurrencyInfo]
  implicit val itemDecoder: JsonDecoder[CurrencyItem] = DeriveJsonDecoder.gen[CurrencyItem]
}
