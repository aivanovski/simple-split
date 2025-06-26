package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.{ExpenseEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.github.ai.split.utils.some
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.util.UUID

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

  def getByUid(uid: UUID): IO[DomainError, ExpenseEntity] = {
    val query = quote {
      querySchema[ExpenseEntity]("expenses")
        .filter(_.uid == lift(uid))
    }

    for {
      expenses <- run(query).mapError(_.toDomainError())
      expense <- ZIO.fromOption(
        expenses.find(_.uid == uid)
      ).mapError(_ => DomainError(message = s"Failed to find expense by uid: $uid".some))
    } yield expense
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[ExpenseEntity]] = {
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
}
