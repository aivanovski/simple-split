package com.github.ai.split.client.utils

import zio.*
import zio.direct.*
import zio.http.*

class Printer {

  def print(response: Response): IO[Throwable, Unit] = defer {
    val statusCode = response.status.code
    val body = response.body.asString.run

    Console.printLine(s"Response[code=$statusCode]:").run

    if (body.nonEmpty) {
      Console.printLine(s"body=$body").run
    }
  }
}
