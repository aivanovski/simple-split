package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.PaidByEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import io.getquill.{SnakeCase, querySchema}
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.sql.SQLException
import java.util.UUID

class PaidByEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[PaidByEntity]] = {
    val query = quote {
      querySchema[PaidByEntity]("paid_by")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByExpenseUid(expenseUid: UUID): IO[DomainError, List[PaidByEntity]] = {
    val query = quote {
      querySchema[PaidByEntity]("paid_by")
        .filter(_.expenseUid == lift(expenseUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[PaidByEntity]] = {
    val query = quote {
      querySchema[PaidByEntity]("paid_by")
        .filter(_.groupUid == lift(groupUid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def add(payers: List[PaidByEntity]): IO[DomainError, List[PaidByEntity]] = {
    val insertQuery = quote {
      liftQuery(payers).foreach { payer =>
        querySchema[PaidByEntity]("paid_by")
          .insertValue(payer)
      }
    }

    val result: IO[SQLException, List[Long]] = run(insertQuery)

    result
      .map(_ => payers)
      .mapError(_.toDomainError())
  }
}
