package com.github.ai.split.presentation.controllers

import com.github.ai.split.utils.*
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AssembleExpenseUseCase}
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.entity.{NewExpenseData, Split, SplitBetweenAll, SplitBetweenMembers}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

class ExpenseController(
  private val accessResolver: AccessResolverService,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val assembleExpenseUseCase: AssembleExpenseUseCase
) {

  def postExpense(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUid <- parseUidFromUrl(request)
      password <- parsePassword(request)
      _ <- accessResolver.canAccessToGroup(groupUid = groupUid, password = password)
      data <- parseData(request)
      expense <- addExpenseUseCase.addExpenseToGroup(
        groupUid = groupUid,
        data = data,
      )
      response <- assembleExpenseUseCase.assembleExpenseDto(expenseUid = expense.uid)
    } yield Response.text(response.toJsonPretty + "\n")
  }

  private def parseData(request: Request): IO[DomainError, NewExpenseData] = {
    for {
      body <- request.body.parse[PostExpenseRequest]
      split <- parseSplit(body)
      paidByUids <- {
        ZIO.collectAll(
          body.paidBy.map(payer => payer.uid.asUid())
        )
      }
    } yield NewExpenseData(
      title = body.title,
      description = body.description.getOrElse(""),
      amount = body.amount,
      paidBy = paidByUids,
      split = split
    )
  }

  private def parseSplit(body: PostExpenseRequest): IO[DomainError, Split] = {
    val isSplitEqually = body.isSplitBetweenAll.getOrElse(false)
    val splitUids = body.splitBetween.getOrElse(List.empty)
    if (!isSplitEqually && splitUids.isEmpty) {
      return ZIO.fail(DomainError(message = "Split is not specified".some))
    }

    if (isSplitEqually && splitUids.nonEmpty) {
      return ZIO.fail(DomainError(message = "Invalid split option specified".some))
    }

    if (!isSplitEqually) {
      ZIO.collectAll(
          splitUids.map(uid => uid.uid.asUid())
        )
        .map(uids => SplitBetweenMembers(userUids = uids))
    } else {
      ZIO.succeed(SplitBetweenAll)
    }
  }

  private def parsePassword(request: Request): IO[DomainError, String] = {
    val password = request.url.queryParamOrElse("password", "")

    ZIO.succeed(password)
  }
}
