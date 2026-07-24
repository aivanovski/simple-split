package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{ExpenseUid, GroupUid, PaidByEntity}
import com.github.ai.split.entity.exception.DomainError
import slick.jdbc.SQLiteProfile.api.*
import zio.{IO, ZIO}

class PaidByEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.PaidByTable) {

  def getAll(): IO[DomainError, List[PaidByEntity]] = {
    queryAll()
  }

  def getByExpenseUid(expenseUid: ExpenseUid): IO[DomainError, List[PaidByEntity]] = {
    query(table => table.expenseUid === expenseUid)
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[PaidByEntity]] = {
    query(table => table.groupUid === groupUid)
  }

  def add(payers: List[PaidByEntity]): IO[DomainError, List[PaidByEntity]] = {
    insertAll(payers)
  }

  def removeByExpenseUid(expenseUid: ExpenseUid): IO[DomainError, Unit] = {
    delete(table => table.expenseUid === expenseUid)
  }
}
