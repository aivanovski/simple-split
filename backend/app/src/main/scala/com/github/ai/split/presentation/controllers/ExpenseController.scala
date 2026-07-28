package com.github.ai.split.presentation.controllers

import com.github.ai.split.utils.*
import com.github.ai.split.domain.usecases.{
  AddExpenseUseCase,
  AssembleExpenseUseCase,
  AssembleGroupResponseUseCase,
  RemoveExpenseUseCase,
  UpdateExpenseUseCase
}
import com.github.ai.split.api.request.{PostExpenseRequest, PutExpenseRequest}
import com.github.ai.split.api.response.{DeleteExpenseResponse, PostExpenseResponse, PutExpenseResponse}
import com.github.ai.split.data.db.model.{ExpenseUid, GroupUid, MemberUid}
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.utils.parsePasswordParam
import com.github.ai.split.entity.{
  MemberReference,
  NewExpense,
  Split,
  SplitBetweenAll,
  SplitBetweenMembers,
  UserReference
}
import zio.*
import zio.http.*
import zio.direct.*

class ExpenseController(
  private val expenseRepository: ExpenseRepository,
  private val accessResolver: AccessResolverService,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val assembleExpenseUseCase: AssembleExpenseUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase,
  private val updateExpenseUseCase: UpdateExpenseUseCase,
  private val removeExpenseUseCase: RemoveExpenseUseCase
) {

  def createExpense(
    password: String,
    body: PostExpenseRequest
  ): IO[DomainError, PostExpenseResponse] =
    defer {
      val groupUid = GroupUid(body.groupUid.parseUid().run)
      accessResolver.canAccessToGroup(groupUid = groupUid, password = password).run

      val paidBy = parsePaidBy(paidByUids = body.paidBy.map(_.uid)).run
      val split = parseSplit(
        isSplitEqually = body.isSplitBetweenAll.getOrElse(false),
        splitUids = body.splitBetween.map(_.uid)
      ).run

      val expense = addExpenseUseCase.addExpenseToGroup(
        groupUid = groupUid,
        newExpense = NewExpense(
          title = body.title.trim,
          description = body.description,
          amount = body.amount,
          paidBy = paidBy,
          split = split
        )
      ).run

      val expenseDto = assembleExpenseUseCase.assembleExpenseDto(expenseUid = expense.uid).run
      PostExpenseResponse(expenseDto)
    }

  def updateExpense(
    expenseId: String,
    password: String,
    data: PutExpenseRequest
  ): IO[DomainError, PutExpenseResponse] =
    defer {
      val expenseUid = ExpenseUid(expenseId.parseUid().run)
      accessResolver.canAccessToExpense(expenseUid = expenseUid, password = password).run

      val paidBy = data.paidBy
      val newPaidBy =
        if (paidBy.nonEmpty) {
          Some(
            parsePaidBy(
              paidByUids = paidBy.map(_.uid)
            ).run
          )
        } else {
          None
        }

      val splitBetween = data.splitBetween
      val isSplitBetweenAll = data.isSplitBetweenAll
      val newSplit =
        if (splitBetween.nonEmpty || isSplitBetweenAll.isDefined) {
          Some(
            parseSplit(
              isSplitEqually = isSplitBetweenAll.getOrElse(false),
              splitUids = splitBetween.map(_.uid)
            ).run
          )
        } else {
          None
        }

      updateExpenseUseCase.updateExpense(
        expenseUid = expenseUid,
        newTitle = data.title.map(_.trim).filter(_.nonEmpty),
        newDescription = data.description.map(_.trim).filter(_.nonEmpty),
        newAmount = data.amount,
        newPaidBy = newPaidBy,
        newSplit = newSplit
      ).run

      val expenseDto = assembleExpenseUseCase.assembleExpenseDto(expenseUid = expenseUid).run
      PutExpenseResponse(expenseDto)
    }

  def removeExpense(
    expenseId: String,
    password: String
  ): IO[DomainError, DeleteExpenseResponse] =
    defer {
      val expenseUid = expenseId.parseUid().map(uid => ExpenseUid(uid)).run
      accessResolver.canAccessToExpense(expenseUid, password).run

      val expense = expenseRepository.getEntityByUid(expenseUid).run
      removeExpenseUseCase.remvoveExpense(expenseUid).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(expense.groupUid).run

      DeleteExpenseResponse(groupDto)
    }

  private def parsePaidBy(
    paidByUids: List[String]
  ): IO[DomainError, List[UserReference]] =
    defer {
      ZIO.foreach(paidByUids) { payer =>
        payer.parseUid().map(uid => MemberReference(MemberUid(uid)))
      }.run
    }

  private def parseSplit(
    isSplitEqually: Boolean,
    splitUids: List[String]
  ): IO[DomainError, Split] =
    defer {
      if (!isSplitEqually) {
        val members = ZIO.foreach(splitUids) { uid =>
          uid.parseUid().map(value => MemberReference(MemberUid(value)))
        }.run
        SplitBetweenMembers(members = members)
      } else {
        SplitBetweenAll
      }
    }
}
