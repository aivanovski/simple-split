package com.github.ai.split.domain

import com.github.ai.split.utils.some
import com.github.ai.split.entity.{CliArguments, HttpProtocol}
import com.github.ai.split.entity.exception.{DomainError, ParsingError}
import zio.*
import zio.direct.*

import scala.collection.mutable

class CliArgumentParser {

  def parse(): ZIO[ZIOAppArgs, DomainError, CliArguments] = {
    defer {
      val args = ZIO.service[ZIOAppArgs].run
      parseArguments(args.getArgs.toList).run
    }
  }

  private def parseArguments(args: List[String]): IO[DomainError, CliArguments] = {
    ZIO
      .attempt {
        var useInMemoryDb = false
        var populateData = false
        var protocol: Option[HttpProtocol] = None

        val queue = mutable.Queue[String]()
        queue.addAll(args)

        while (queue.nonEmpty) {
          val optionName = queue.removeHead()
          optionName match {
            case CliOptions.InMemoryDb.cliName => useInMemoryDb = true
            case CliOptions.PopulateData.cliName => populateData = true
            case CliOptions.Protocol.cliName => {
              val protocolValue = queue
                .removeHeadOption()
                .flatMap(value => HttpProtocol.fromString(value))

              protocolValue match {
                case Some(p) => protocol = Some(p)
                case None =>
                  throw new ParsingError(
                    s"Invalid option: ${CliOptions.Protocol.cliName}. Expected 'http' or 'https'"
                  )
              }
            }

            case _ =>
              throw new ParsingError(s"Invalid option specified: '$optionName'")
          }
        }

        if (protocol.isEmpty) {
          throw new ParsingError(s"Option '${CliOptions.Protocol.cliName}' is required ")
        }

        CliArguments(
          isUseInMemoryDatabase = useInMemoryDb,
          isPopulateTestData = populateData,
          protocol = protocol.get
        )
      }
      .mapError(error => DomainError(cause = error.some))
  }

  private enum CliOptions(val cliName: String) {
    case InMemoryDb extends CliOptions("--in-memory-db")
    case PopulateData extends CliOptions("--populate-data")
    case Protocol extends CliOptions("--protocol")
  }
}
