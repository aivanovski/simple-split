package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.{PaidByEntity, SplitBetweenEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import io.getquill.{SnakeCase, querySchema}
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.IO

import java.sql.SQLException
import java.util.UUID

class SplitBetweenEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[SplitBetweenEntity]] = {
    val query = quote {
      querySchema[SplitBetweenEntity]("split_between")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByExpenseUid(expenseUid: UUID): IO[DomainError, List[SplitBetweenEntity]] = {
    val query = quote {
      querySchema[SplitBetweenEntity]("split_between")
        .filter(_.expenseUid == lift(expenseUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[SplitBetweenEntity]] = {
    val query = quote {
      querySchema[SplitBetweenEntity]("split_between")
        .filter(_.groupUid == lift(groupUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def add(splits: List[SplitBetweenEntity]): IO[DomainError, List[SplitBetweenEntity]] = {
    val insertQuery = quote {
      liftQuery(splits).foreach { split =>
        querySchema[SplitBetweenEntity]("split_between")
          .insertValue(split)
      }
    }

    val result: IO[SQLException, List[Long]] = run(insertQuery)

    result
      .map(_ => splits)
      .mapError(_.toDomainError())
  }
}
