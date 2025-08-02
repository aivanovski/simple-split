package com.github.ai.split.client.utils

import zio.http.*
import zio.json.*
import zio.*
import zio.direct.*

extension (response: Response) {
  def parseBody[T](implicit decoder: JsonDecoder[T]): IO[Throwable, T] = {
    defer {
      val jsonBody = response.body.asString.run

      ZIO
        .fromEither(
          jsonBody.fromJson[T](using decoder).left.map(errorMessage => Exception(errorMessage))
        )
        .run
    }
  }
}
