package com.github.ai.split.domain

import com.github.ai.split.data.db.dao.{GroupEntityDao}
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}

import java.util.UUID

class AccessResolverService(
  private val expenseRepository: ExpenseRepository,
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao
) {

  def canAccessToGroups(
    groupUids: List[UUID],
    passwords: List[String]
  ): IO[DomainError, Unit] = {
    ZIO.collectAll(
        groupUids.zip(passwords)
          .map((groupUid, password) => canAccessToGroup(groupUid, password))
      )
      .map(_ => ())
  }

  def canAccessToExpense(
    expenseUid: UUID,
    password: String
  ): IO[DomainError, Unit] = {
    for {
      expense <- expenseRepository.getEntityByUid(uid = expenseUid)
      _ <- canAccessToGroup(groupUid = expense.groupUid, password = password)
    } yield ()
  }

  def canAccessToGroup(
    groupUid: UUID,
    password: String
  ): IO[DomainError, Unit] = {
    for {
      group <- groupDao.getByUid(groupUid)
      _ <- isPasswordMatch(password = password, passwordHash = group.passwordHash)
    } yield ()
  }

  private def isPasswordMatch(
    password: String,
    passwordHash: Option[String]
  ): IO[DomainError, Unit] = {
    if (password.isEmpty && passwordHash.isEmpty) {
      ZIO.succeed(())
    } else {
      passwordService.verifyPassword(
        password = password,
        hashedPassword = passwordHash.getOrElse("")
      )
    }
  }
}
