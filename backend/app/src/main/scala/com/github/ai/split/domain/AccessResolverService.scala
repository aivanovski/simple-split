package com.github.ai.split.domain

import com.github.ai.split.data.db.dao.{GroupEntityDao, MemberEntityDao}
import com.github.ai.split.data.db.model.Acknowledgement.{CONFIRMED, REQUESTED}
import com.github.ai.split.data.db.model.{ExpenseUid, GroupUid, MemberUid, PasswordHash, UserEntity, UserUid}
import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository, UserRepository}
import com.github.ai.split.entity.{Access, AccessResolutionResult, Reason}
import com.github.ai.split.entity.Access.{DENIED, GRANTED}
import com.github.ai.split.entity.exception.{DomainError, GroupAccessDeniedError}
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

class AccessResolverService(
  private val expenseRepository: ExpenseRepository,
  private val passwordService: PasswordService,
  private val groupRepository: GroupRepository,
  private val userRepository: UserRepository,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: MemberEntityDao
) {

  def canAccessToGroup(
    userUid: UserUid,
    groupUid: GroupUid
  ): IO[DomainError, Unit] = defer {
    val result = canAccessToGroups(userUid = userUid, groupUids = List(groupUid)).run

    val isAccessGranted = result.headOption.exists { access =>
      access.access == GRANTED
    }

    if (isAccessGranted) {
      ZIO.unit.run
    } else {
      ZIO.fail(GroupAccessDeniedError(groupUid = groupUid)).run
    }
  }

  def canAccessToGroups(
    userUid: UserUid,
    groupUids: List[GroupUid]
  ): IO[DomainError, List[AccessResolutionResult[GroupUid]]] = defer {
    val groups = groupRepository.getByUids(groupUids).run
    val user = userRepository.getByUid(userUid).run

    groups.map { group =>
      val isAlreadyMember = group.members.exists { member =>
        member.user.isDefined && member.user.get.uid == user.uid
      }

      val wasRequestedToBeMember = group.members.exists { member =>
        val email = member.member.email.getOrElse("").toLowerCase
        val acknowledgement = member.member.acknowledgement

        email == user.email.toLowerCase && (acknowledgement == REQUESTED || acknowledgement == CONFIRMED)
      }

      if (isAlreadyMember || wasRequestedToBeMember) {
        AccessResolutionResult(
          uid = group.entity.uid,
          access = Access.GRANTED,
          reason = None
        )
      } else {
        AccessResolutionResult(
          uid = group.entity.uid,
          access = Access.DENIED,
          reason = Some(Reason.NOT_FOUND)
        )
      }
    }
  }

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
                      access = if (passwordService.isPasswordMatch(password, group.passwordHash)) {
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
                      reason = Some(Reason.NOT_FOUND)
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
    passwordHash: PasswordHash
  ): IO[DomainError, Unit] = {
    val isMatch = passwordService.isPasswordMatch(
      password = password,
      hashedPassword = passwordHash
    )

    if (isMatch) {
      ZIO.unit
    } else {
      ZIO.fail(DomainError(message = "Password doesn't match".some))
    }
  }
}
