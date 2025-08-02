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
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.entity.db.{ExpenseUid, GroupUid, MemberUid}
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
import zio.json.*
import zio.direct.*

import java.util.UUID

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
    request: Request
  ): IO[DomainError, Response] = {
    for {
      body <- request.body.parse[PostExpenseRequest]
      groupUid <- body.groupUid.parseUid().map(uid => GroupUid(uid))
      password <- parsePasswordParam(request)
      _ <- accessResolver.canAccessToGroup(groupUid = groupUid, password = password)

      paidBy <- parsePaidBy(paidByUids = body.paidBy.map(_.uid))
      split <- parseSplit(
        isSplitEqually = body.isSplitBetweenAll.getOrElse(false),
        splitUids = body.splitBetween.getOrElse(List.empty).map(_.uid)
      )

      expense <- addExpenseUseCase.addExpenseToGroup(
        groupUid = groupUid,
        newExpense = NewExpense(
          title = body.title.trim,
          description = body.description.map(_.trim).getOrElse(""),
          amount = body.amount,
          paidBy = paidBy,
          split = split
        )
      )
      expenseDto <- assembleExpenseUseCase.assembleExpenseDto(expenseUid = expense.uid)
    } yield Response.json(PostExpenseResponse(expenseDto).toJsonPretty)
  }

  def updateExpense(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      expenseUid <- parseUidFromUrl(request).map(uid => ExpenseUid(uid))
      password <- parsePasswordParam(request)
      _ <- accessResolver.canAccessToExpense(expenseUid = expenseUid, password = password)

      data <- request.body.parse[PutExpenseRequest]

      newPaidBy <-
        if (data.paidBy.isDefined) {
          parsePaidBy(
            paidByUids = data.paidBy.getOrElse(List.empty).map(_.uid)
          ).map(paidBy => Some(paidBy))
        } else {
          ZIO.succeed(None)
        }

      newSplit <-
        if (data.splitBetween.isDefined || data.isSplitBetweenAll.isDefined) {
          parseSplit(
            isSplitEqually = data.isSplitBetweenAll.getOrElse(false),
            splitUids = data.splitBetween.getOrElse(List.empty).map(_.uid)
          )
            .map(split => Some(split))
        } else {
          ZIO.succeed(None)
        }

      _ <- updateExpenseUseCase.updateExpense(
        expenseUid = expenseUid,
        newTitle = data.title.map(_.trim).filter(_.nonEmpty),
        newDescription = data.description.map(_.trim).filter(_.nonEmpty),
        newAmount = data.amount,
        newPaidBy = newPaidBy,
        newSplit = newSplit
      )

      expenseDto <- assembleExpenseUseCase.assembleExpenseDto(expenseUid = expenseUid)
    } yield Response.json(PutExpenseResponse(expenseDto).toJsonPretty)
  }

  def removeExpense(
    request: Request
  ): IO[DomainError, Response] = {
    defer {
      val password = parsePasswordParam(request).run
      val expenseUid = parseUidFromUrl(request).map(uid => ExpenseUid(uid)).run
      accessResolver.canAccessToExpense(expenseUid, password).run

      val expense = expenseRepository.getEntityByUid(expenseUid).run
      removeExpenseUseCase.remvoveExpense(expenseUid).run

      val groupDto = assembleGroupUseCase.assembleGroupDto(expense.groupUid).run

      Response.json(DeleteExpenseResponse(groupDto).toJsonPretty)
    }
  }

  private def parsePaidBy(
    paidByUids: List[String]
  ): IO[DomainError, List[UserReference]] = {
    ZIO.collectAll(
      paidByUids.map { payer =>
        payer.parseUid().map(uid => MemberReference(MemberUid(uid)))
      }
    )
  }

  private def parseSplit(
    isSplitEqually: Boolean,
    splitUids: List[String]
  ): IO[DomainError, Split] = {
    if (!isSplitEqually) {
      ZIO
        .collectAll(
          splitUids.map { uid =>
            uid.parseUid().map(uid => MemberReference(MemberUid(uid)))
          }
        )
        .map(uids => SplitBetweenMembers(members = uids))
    } else {
      ZIO.succeed(SplitBetweenAll)
    }
  }
}
