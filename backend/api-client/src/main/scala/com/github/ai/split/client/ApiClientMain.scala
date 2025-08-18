package com.github.ai.split.client

import com.github.ai.split.client.ApiClientMain.getArgs
import com.github.ai.split.client.utils.Printer
import com.github.ai.split.client.utils.parseOptionsIntoMap
import zio.*
import zio.direct.*
import zio.http.*

object ApiClientMain extends ZIOAppDefault {

  private val HelpText =
    """
      |Commands:
      |
      |group                                                 Gets default group
      |group [GROUP_UID]                                     Gets group by GROUP_UID
      |gen-group                                             Generate new test group with members and expenses
      |update-group [GROUP_UID] [OPTIONS]                    Updates group (requires at least one: --title, --password, --description)
      |
      |post-expense                                          Creates new expense in default group
      |gen-expense                                           Generates new expense in default group
      |delete-expense [EXPENSE_UID]                          Deletes expense by EXPENSE_UID
      |
      |post-member [GROUP_UID] [USER_NAME]                   Create new member with USER_NAME in GROUP_UID
      |gen-members [GROUP_UID]
      |delete-member [MEMBER_UID]                            Deletes member by MEMBER_UID
      |help                                                  Print help
      |""".stripMargin

  class InvalidCliArgumentException(message: String) extends Exception(message)

  override def run: ZIO[ZIOAppArgs, Any, ExitCode] = {
    val application = for {
      arguments <- getArgs.map(_.toList.mkString(" "))
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
      case "group" => api.getGroup(uid = Groups.TripToDisneyLand).run
      case s"group $groupUid" => api.getGroup(uid = groupUid).run
      case s"gen-group" => api.postGroup().run

      case "post-expense" => api.postExpense().run
      case "gen-expense" => api.postExpense(title = Data.newExpenseTitle()).run
      case s"delete-expense $expenseUid" => api.deleteExpense(expenseUid = expenseUid).run

      case s"post-member $groupUid $userName" => api.postMember(groupUid = groupUid, userName = userName).run
      case s"gen-members $groupUid" => {
        api.postMember(groupUid = groupUid, userName = "Mickey").run
        api.postMember(groupUid = groupUid, userName = "Donald").run
      }
      case s"delete-member $memberUid" => api.deleteMember(memberUid = memberUid).run
      case s"update-group $groupUid $other" =>
        val map = parseOptionsIntoMap(other.split(" ").toList, Set("--title", "--password", "--description")).run
        api.updateGroup(
          groupUid = groupUid,
          title = map.get("--title"),
          password = map.get("--password"),
          description = map.get("--description")
        ).run
      case _ => ZIO.fail(InvalidCliArgumentException(s"Illegal arguments: $arguments")).run
    }

    printer.print(response).run

    ExitCode.success
  }
}
