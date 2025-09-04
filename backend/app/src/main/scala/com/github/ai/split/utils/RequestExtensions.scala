package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.IO
import zio.ZIO
import zio.http.Request

import java.util.UUID

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
      ZIO.fail(new DomainError(message = Some("Invalid id parameter")))
    }
  }
}

def parseUidFromUrl(request: Request): IO[DomainError, UUID] = {
  for {
    groupUidStr <- request.getLastUrlParameter()
    groupUid <- groupUidStr.parseUid()
  } yield groupUid
}

def parsePasswordParam(request: Request): IO[DomainError, String] = {
  val password = request.url.queryParamOrElse("password", "")
  ZIO.succeed(password)
}
