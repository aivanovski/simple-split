package com.github.ai.split.presentation.controllers

import com.github.ai.split.utils.*
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AssembleExpenseUseCase}
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.response.PostExpenseResponse
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.entity.{NewExpense, Split, SplitBetweenAll, SplitBetweenMembers, UidReference, UserReference}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

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
      newExpense <- parseExpense(request)
      expense <- addExpenseUseCase.addExpenseToGroup(
        groupUid = groupUid,
        newExpense = newExpense,
      )
      expenseDto <- assembleExpenseUseCase.assembleExpenseDto(expenseUid = expense.uid)
    } yield Response.text(
      text = PostExpenseResponse(expenseDto).toJsonPretty
    )
  }

  private def parseExpense(request: Request): IO[DomainError, NewExpense] = {
    for {
      body <- request.body.parse[PostExpenseRequest]
      paidBy <- parsePaidBy(body)
      split <- parseSplit(body)
    } yield NewExpense(
      title = body.title,
      description = body.description.getOrElse(""),
      amount = body.amount,
      paidBy = paidBy,
      split = split
    )
  }

  private def parsePaidBy(body: PostExpenseRequest): IO[DomainError, List[UUID]] = {
    ZIO.collectAll(
      body.paidBy.map {
        payer => payer.uid.asUid()
      }
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
          splitUids
            .map(uid => uid.uid.asUid())
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
