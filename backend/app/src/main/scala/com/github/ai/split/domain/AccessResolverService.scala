package com.github.ai.split.domain

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.data.db.repository.ExpenseRepository
import com.github.ai.split.entity.{Access, AccessResolutionResult}
import com.github.ai.split.entity.Access.{DENIED, GRANTED}
import com.github.ai.split.entity.Reason.NOT_FOUND
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import com.github.ai.split.entity.db.{ExpenseUid, GroupUid, MemberUid, UserUid}
import zio.*
import zio.direct.*

class AccessResolverService(
  private val expenseRepository: ExpenseRepository,
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def canAccessToGroups(
    groupUids: List[GroupUid],
    passwords: List[String]
  ): IO[DomainError, List[AccessResolutionResult[GroupUid]]] = {
    val uidsAndPasswords = groupUids.zip(passwords)

    for {
      result <- ZIO
        .collectAll(
          uidsAndPasswords
            .map { (groupUid, password) =>
              groupDao
                .findByUid(groupUid)
                .map {
                  case Some(group) =>
                    AccessResolutionResult(
                      uid = groupUid,
                      access = if (passwordService.isPasswordMatch(password, group.passwordHash.getOrElse(""))) {
                        GRANTED
                      } else {
                        DENIED
                      },
                      reason = None
                    )
                  case None =>
                    AccessResolutionResult(
                      uid = groupUid,
                      access = DENIED,
                      reason = Some(NOT_FOUND)
                    )
                }
            }
        )
    } yield result
  }

  def canAccessToExpense(
    expenseUid: ExpenseUid,
    password: String
  ): IO[DomainError, Unit] = {
    defer {
      val expense = expenseRepository.getEntityByUid(uid = expenseUid).run
      canAccessToGroup(groupUid = expense.groupUid, password = password).run

      ()
    }
  }

  def canAccessToGroup(
    groupUid: GroupUid,
    password: String
  ): IO[DomainError, Unit] = {
    defer {
      val group = groupDao.getByUid(groupUid).run
      isPasswordMatch(password = password, passwordHash = group.passwordHash).run

      ()
    }
  }

  def canAccessToMember(
    memberUid: MemberUid,
    password: String
  ): IO[DomainError, Unit] = {
    defer {
      val member = groupMemberDao.getByUid(uid = memberUid).run
      val group = groupDao.getByUid(uid = member.groupUid).run
      isPasswordMatch(password = password, passwordHash = group.passwordHash).run

      ()
    }
  }

  private def isPasswordMatch(
    password: String,
    passwordHash: Option[String]
  ): IO[DomainError, Unit] = {
    if (password.isEmpty && passwordHash.isEmpty) {
      ZIO.unit
    } else {
      val isMatch = passwordService.isPasswordMatch(
        password = password,
        hashedPassword = passwordHash.getOrElse("")
      )

      if (isMatch) {
        ZIO.unit
      } else {
        ZIO.fail(DomainError(message = "Password doesn't match".some))
      }
    }
  }
}
