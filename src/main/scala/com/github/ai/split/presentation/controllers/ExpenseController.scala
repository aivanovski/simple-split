package com.github.ai.split.presentation.controllers

import com.github.ai.split.utils.*
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AssembleExpenseUseCase}
import com.github.ai.split.entity.api.request.PostExpenseRequest
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.NewExpense
import com.github.ai.split.utils.getLastUrlParameter
import com.github.ai.split.entity.db.UserEntity
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

class ExpenseController(
  private val addExpenseUseCase: AddExpenseUseCase,
  private val assembleExpenseUseCase: AssembleExpenseUseCase
) {

  def postExpense(
    user: UserEntity,
    request: Request
  ): IO[DomainError, Response] = {
    for {
      data <- parseData(request)
      groupUidStr <- request.getLastUrlParameter()
      groupUid <- groupUidStr.asUid()
      expense <- addExpenseUseCase.addExpenseToGroup(user, groupUid, data)
      response <- assembleExpenseUseCase.assembleExpenseDto(expenseUid = expense.uid)
    } yield Response.text(response.toJsonPretty + "\n")
  }

  private def parseData(request: Request): IO[DomainError, NewExpense] = {
    for {
      body <- request.body.parse[PostExpenseRequest]
      paidByUids <- {
        ZIO.collectAll(
          body.paidBy.map(payer => payer.uid.asUid())
        )
      }
      splitBetweenUids <- {
        ZIO.collectAll(
          body.splitBetween.map(split => split.uid.asUid())
        )
      }
    } yield NewExpense(
      title = body.title,
      description = body.description.getOrElse(""),
      amount = body.amount,
      paidBy = paidByUids,
      splitBetween = splitBetweenUids
    )
  }
}
