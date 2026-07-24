package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{ExpenseUid, GroupUid, SplitBetweenEntity}
import com.github.ai.split.entity.exception.DomainError
import slick.jdbc.SQLiteProfile.api.*
import zio.IO

class SplitBetweenEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.SplitBetweenTable) {

  def getAll(): IO[DomainError, List[SplitBetweenEntity]] = {
    queryAll()
  }

  def getByExpenseUid(expenseUid: ExpenseUid): IO[DomainError, List[SplitBetweenEntity]] = {
    query(table => table.expenseUid === expenseUid)
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[SplitBetweenEntity]] = {
    query(table => table.groupUid === groupUid)
  }

  def add(splits: List[SplitBetweenEntity]): IO[DomainError, List[SplitBetweenEntity]] = {
    insertAll(splits)
  }

  def removeByExpenseUid(expenseUid: ExpenseUid): IO[DomainError, Unit] = {
    delete(table => table.expenseUid === expenseUid)
  }
}
