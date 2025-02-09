package com.github.ai.split.presentation.controllers

import com.github.ai.split.utils.*
import com.github.ai.split.data.{ExpenseRepository, GroupRepository, UserRepository}
import com.github.ai.split.domain.usecases.AddExpenseUseCase
import com.github.ai.split.entity.{Expense, User}
import com.github.ai.split.entity.api.request.PostExpenseRequest
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.Group
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class ExpenseController(
  private val expenseRepository: ExpenseRepository,
  private val groupRepository: GroupRepository,
  private val userRepository: UserRepository,
  private val addExpenseUseCase: AddExpenseUseCase
) {

  def postExpense(
    groupId: String,
    user: User,
    request: Request
  ): ZIO[Any, DomainError, Response] = {
     for
      body <- request.body.parse[PostExpenseRequest]
      groupUid <- groupId.asUid()
      group <- groupRepository.getByUid(groupUid)
      paidBy <- ZIO.collectAll(body.paidBy.map(uid => uid.uid.asUid()))
      splitBetween <- ZIO.collectAll(body.splitBetween.map(uid => uid.uid.asUid()))
      newGroup <- addExpense(group, body, paidBy, splitBetween)
      userUidToUserMap <- userRepository.getUserUidToUserMap()
      response <- toGroupDto(newGroup, userUidToUserMap)
    yield 
      Response.text(response.toJsonPretty + "\n")
  }

  private def addExpense(
    group: Group,
    body: PostExpenseRequest,
    paidBy: List[UUID],
    splitBetween: List[UUID]
  ): IO[DomainError, Group] = {
    val expense = Expense(
      uid = UUID.randomUUID(),
      groupUid = group.uid,
      title = body.title,
      description = body.description.getOrElse(""),
      amount = body.amount,
      paidBy = paidBy,
      splitBetween = splitBetween
    )

    addExpenseUseCase.addExpenseToGroup(expense, group)
  }
}
