package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{ExpenseEntity, ExpenseUid, GroupUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class ExpenseEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.ExpenseTable) {

  def getByUid(uid: ExpenseUid): IO[DomainError, ExpenseEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { expenseOption =>
        ZIO
          .fromOption(expenseOption)
          .mapError(_ => DomainError(message = s"Failed to find expense by uid: $uid".some))
      }
  }

  def getByUids(uids: List[ExpenseUid]): IO[DomainError, List[ExpenseEntity]] = {
    val uidSet = uids.toSet
    query(table => table.uid inSet uidSet)
  }

  def getByGroupUids(groupUids: List[GroupUid]): IO[DomainError, List[ExpenseEntity]] = {
    val uidSet = groupUids.toSet
    query(table => table.groupUid inSet uidSet)
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[ExpenseEntity]] = {
    query(table => table.groupUid === groupUid)
  }

  def add(expense: ExpenseEntity): IO[DomainError, ExpenseEntity] = {
    insert(expense)
  }

  def update(expense: ExpenseEntity): IO[DomainError, ExpenseEntity] = {
    updateOne(table => table.uid === expense.uid, entity = expense)
  }

  def delete(uid: ExpenseUid): IO[DomainError, Unit] = {
    deleteOne(table => table.uid === uid)
  }
}
