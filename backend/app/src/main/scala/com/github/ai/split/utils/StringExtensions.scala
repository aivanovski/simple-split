package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.json.JsonDecoder
import zio.*
import zio.json.*

extension (str: String) {
  def removeSuffixAfter(symbol: String): String = {
    val lastIndex = str.lastIndexOf(symbol)
    if lastIndex >= 0 then str.substring(0, lastIndex) else str
  }

  def parseJson[T](implicit decoder: JsonDecoder[T]): IO[DomainError, T] = {
    ZIO.fromEither(
      str
        .fromJson[T](using decoder)
        .left
        .map(error => new DomainError(message = error.some))
    )
  }
}
