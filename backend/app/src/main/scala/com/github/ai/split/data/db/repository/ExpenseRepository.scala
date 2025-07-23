package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.entity.ExpenseWithRelations
import com.github.ai.split.entity.db.ExpenseEntity
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class ExpenseRepository(
  private val expenseDao: ExpenseEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao
) {

  def getByUid(uid: UUID): IO[DomainError, ExpenseWithRelations] = {
    for {
      expense <- expenseDao.getByUid(uid)
      paidBy <- paidByDao.getByExpenseUid(uid)
      splitBetween <- splitBetweenDao.getByExpenseUid(uid)
    } yield ExpenseWithRelations(expense, paidBy, splitBetween)
  }

  def getEntitiesByGroupUids(groupUids: List[UUID]): IO[DomainError, List[ExpenseEntity]] = {
    expenseDao.getByGroupUids(groupUids = groupUids)
  }

  def getEntityByUid(uid: UUID): IO[DomainError, ExpenseEntity] = {
    expenseDao.getByUid(uid)
  }

  def getEntitiesByGroupUid(groupUid: UUID): IO[DomainError, List[ExpenseEntity]] = {
    expenseDao.getByGroupUid(groupUid)
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[ExpenseWithRelations]] = {
    for {
      expenses <- expenseDao.getByGroupUid(groupUid)
      paidByAll <- paidByDao.getByGroupUid(groupUid)
      splitBetweenAll <- splitBetweenDao.getByGroupUid(groupUid)
    } yield {
      val paidByMap = paidByAll.groupBy(_.expenseUid)
      val splitBetweenMap = splitBetweenAll.groupBy(_.expenseUid)

      expenses.map { expense =>
        val paidBy = paidByMap.getOrElse(expense.uid, List.empty)
        val splitBetween = splitBetweenMap.getOrElse(expense.uid, List.empty)
        ExpenseWithRelations(expense, paidBy, splitBetween)
      }
    }
  }

  def add(
    expense: ExpenseWithRelations
  ): IO[DomainError, ExpenseWithRelations] = {
    for {
      _ <- expenseDao.add(expense.entity)
      _ <- paidByDao.add(expense.paidBy)
      _ <- splitBetweenDao.add(expense.splitBetween)
    } yield expense
  }

  def update(
    expense: ExpenseWithRelations
  ): IO[DomainError, ExpenseWithRelations] = {
    val expenseUid = expense.entity.uid

    for {
      _ <- paidByDao.removeByExpenseUid(expenseUid)
      _ <- splitBetweenDao.removeByExpenseUid(expenseUid)

      _ <- expenseDao.update(expense.entity)
      _ <- paidByDao.add(expense.paidBy)
      _ <- splitBetweenDao.add(expense.splitBetween)
    } yield expense
  }

  def deleteByUid(uid: UUID): IO[DomainError, Unit] = {
    for {
      _ <- paidByDao.removeByExpenseUid(uid)
      _ <- splitBetweenDao.removeByExpenseUid(uid)
      _ <- expenseDao.delete(uid)
    } yield ()
  }
}