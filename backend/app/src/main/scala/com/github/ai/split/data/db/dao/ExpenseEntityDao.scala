package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.{ExpenseEntity, ExpenseUid, GroupUid, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.github.ai.split.utils.some
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

class ExpenseEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[ExpenseEntity]] = {
    val query = quote {
      querySchema[ExpenseEntity]("expenses")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByUid(uid: ExpenseUid): IO[DomainError, ExpenseEntity] = {
    val query = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(_.uid == lift(uid))
    }

    for {
      expenses <- run(query).mapError(_.toDomainError())
      expense <- ZIO
        .fromOption(
          expenses.find(_.uid == uid)
        )
        .mapError(_ => DomainError(message = s"Failed to find expense by uid: $uid".some))
    } yield expense
  }

  def getByUids(uids: List[ExpenseUid]): IO[DomainError, List[ExpenseEntity]] = {
    val uidSet = uids.toSet

    val query = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(expense => liftQuery(uidSet).contains(expense.uid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUids(groupUids: List[GroupUid]): IO[DomainError, List[ExpenseEntity]] = {
    val groupUidSet = groupUids.toSet

    val query = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(expense => liftQuery(groupUidSet).contains(expense.groupUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[ExpenseEntity]] = {
    val query = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(_.groupUid == lift(groupUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def add(expense: ExpenseEntity): IO[DomainError, ExpenseEntity] = {
    run(
      quote {
        querySchema[ExpenseEntity]("expenses")
          .insertValue(lift(expense))
      }
    )
      .map(_ => expense)
      .mapError(_.toDomainError())
  }

  def update(expense: ExpenseEntity): IO[DomainError, ExpenseEntity] = {
    val updateQuery = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(_.uid == lift(expense.uid))
        .updateValue(lift(expense))
    }

    run(updateQuery)
      .map(_ => expense)
      .mapError(_.toDomainError())
  }

  def delete(uid: ExpenseUid): IO[DomainError, Unit] = {
    val deleteQuery = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(_.uid == lift(uid))
        .delete
    }

    run(deleteQuery)
      .map(_ => ())
      .mapError(_.toDomainError())
  }
}
