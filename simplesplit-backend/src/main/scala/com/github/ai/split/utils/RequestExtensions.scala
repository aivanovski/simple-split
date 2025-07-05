package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.IO
import zio.ZIO
import zio.http.{Body, Request}
import zio.json.*

import java.util.UUID

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

extension (request: Request) {
  def getLastUrlParameter(): ZIO[Any, DomainError, String] = {
    val parameter = request.url.toString
      .removeSuffixAfter("?")
      .split("/")
      .filter(_.nonEmpty)
      .lastOption
      .getOrElse("")

    if (parameter.nonEmpty) {
      ZIO.succeed(parameter)
    } else {
      ZIO.fail(new DomainError(message = Some("Invalid groupId")))
    }
  }
}

def parseUidFromUrl(request: Request): IO[DomainError, UUID] = {
  for {
    groupUidStr <- request.getLastUrlParameter()
    groupUid <- groupUidStr.asUid()
  } yield groupUid
}

def parsePasswordParam(request: Request): IO[DomainError, String] = {
  val password = request.url.queryParamOrElse("password", "")
  ZIO.succeed(password)
}