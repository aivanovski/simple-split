package com.github.ai.split.client

import com.github.ai.split.client.ApiClientMain.getArgs
import com.github.ai.split.client.utils.Printer
import zio.*
import zio.direct.*
import zio.http.*

object ApiClientMain extends ZIOAppDefault {

  private val HelpText =
    """
      |Commands:
      |
      |group                                                 Gets default group
      |post-group                                            Creates new group
      |post-expense                                          Creates new expense in default group
      |gen-expense                                           Generates new expense in default group
      |help                                                  Print help
      |""".stripMargin

  class InvalidCliArgumentException(message: String) extends Exception(message)

  override def run: ZIO[ZIOAppArgs, Any, ExitCode] = {
    val application = for {
      arguments <- getArgs.map(_.toList.mkString)
      result <- processArguments(arguments)
        .provide(
          Client.default,
          Scope.default,
          ZLayer.succeed(Printer()),
          ZLayer.fromFunction(ApiClient(_))
        )
    } yield result

    application
      .catchAll { error =>
        defer {
          Console.printLine(s"Error: $error").run

          if (error.isInstanceOf[InvalidCliArgumentException]) {
            Console.printLine(HelpText).run
          }

          ExitCode.failure
        }
      }
  }

  private def processArguments(arguments: String) = defer {
    val api = ZIO.service[ApiClient].run
    val printer = ZIO.service[Printer].run

    val response = arguments match {
      case "group" => api.getGroup().run
      case "post-group" => api.postGroup().run
      case "post-expense" => api.postExpense().run
      case "gen-expense" => api.postExpense(title = Data.newExpenseTitle()).run
      case _ => ZIO.fail(InvalidCliArgumentException(s"Illegal arguments: $arguments")).run
    }

    printer.print(response).run

    ExitCode.success
  }

}