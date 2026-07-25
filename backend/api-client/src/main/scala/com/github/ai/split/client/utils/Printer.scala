package com.github.ai.split.client.utils

import zio.*
import zio.direct.*
import zio.http.*
import zio.json.*
import zio.json.ast.Json

class Printer {

  private def prettyBody(body: String): String =
    body.fromJson[Json].fold(_ => body, _.toJsonPretty)

  def print(response: Response): IO[Throwable, Unit] = defer {
    val statusCode = response.status.code
    val body = response.body.asString.run

    Console.printLine(s"Response[code=$statusCode]:").run

    if (body.nonEmpty) {
      Console.printLine(s"body=${prettyBody(body)}").run
    }
  }
}
