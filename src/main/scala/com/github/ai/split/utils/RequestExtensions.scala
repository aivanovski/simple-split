package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.IO
import zio.ZIO
import zio.http.{Body, Request}
import zio.json.*

extension (request: Request) {

  def getLastUrlParameter(): IO[DomainError, String] = {
    val parameter = request.url.toString.split("/")
      .filter(_.nonEmpty)
      .lastOption
      .getOrElse("")

    if (parameter.nonEmpty) {
      ZIO.succeed(parameter)
    } else {
      ZIO.fail(new DomainError(message = "Invalid groupId".some))
    }
  }
}

extension (body: Body) {

  def parse[T](implicit decoder: JsonDecoder[T]): IO[DomainError, T] = {
    for
      text <- body.asString.mapError(error => new DomainError(cause = error.some))

      dto <- ZIO.fromEither(
        text.fromJson[T](decoder)
          .left.map(error => new DomainError(message = error.some))
      )
    yield dto

  }
}