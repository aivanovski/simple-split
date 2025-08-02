package com.github.ai.split.domain

import com.github.ai.split.utils.some
import com.github.ai.split.entity.CliArguments
import com.github.ai.split.entity.exception.DomainError
import zio.*

class CliArgumentParser {

  def parse(): ZIO[ZIOAppArgs, DomainError, CliArguments] = {
    for {
      appArgs <- ZIO.service[ZIOAppArgs]
      parsedArgs <- parseArguments(appArgs.getArgs.toArray)
    } yield parsedArgs
  }

  private def parseArguments(args: Array[String]): IO[DomainError, CliArguments] = {
    ZIO.foldLeft(args)(CliArguments()) { (acc, arg) =>
      arg match {
        case "--use-test-db" => ZIO.succeed(acc.copy(isUseTestDatabase = true))
        case arg =>
          ZIO.fail(DomainError(message = s"Unexpected argument: $arg".some))
      }
    }
  }
}
