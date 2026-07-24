package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{
  GroupEntityDao,
  GroupMembershipEntityDao,
  PaidByEntityDao,
  SplitBetweenEntityDao,
  MemberEntityDao
}
import com.github.ai.split.data.db.model.{GroupEntity, GroupMembershipEntity, GroupUid, MemberUid, MembershipUid, Timestamp}
import com.github.ai.split.domain.usecases.AddMembersUseCase
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import java.time.{LocalDateTime, ZoneOffset}
import java.util.UUID

class UpdateGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMembershipEntityDao,
  private val userDao: MemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val addMemberUseCase: AddMembersUseCase,
  private val removeMembersUseCase: RemoveMembersUseCase,
  private val validateCurrencyUseCase: ValidateCurrencyUseCase
) {

  def updateGroup(
    groupUid: GroupUid,
    newPassword: Option[String],
    newTitle: Option[String],
    newDescription: Option[String],
    newCurrencyIsoCode: Option[String],
    newMemberUids: Option[List[MemberUid]]
  ): IO[DomainError, GroupUid] = {
    for {
      _ <- isCurrencyIsoCodeValid(newCurrencyIsoCode)

      group <- groupDao.getByUid(uid = groupUid)
      currentMembers <- groupMemberDao.getByGroupUid(groupUid = groupUid)

      membersToAdd <- getMembersToAdd(
        currentMembers = currentMembers,
        newMemberUids = newMemberUids
      )
      _ <-
        if (membersToAdd.nonEmpty) {
          addMemberUseCase.canAddMembers(groupUid = groupUid, userUids = membersToAdd)
        } else {
          ZIO.succeed(())
        }

      membersToRemove <- getMembersToRemove(
        currentMembers = currentMembers,
        newMemberUids = newMemberUids
      )
      _ <-
        if (membersToRemove.nonEmpty) {
          removeMembersUseCase.canRemoveMembers(groupUid = groupUid, memberUids = membersToRemove)
        } else {
          ZIO.succeed(())
        }

      _ <- updateMembers(groupUid = groupUid, newMembersOption = newMemberUids)

      _ <- {
        groupDao.update(
          GroupEntity(
            uid = groupUid,
            title = newTitle.getOrElse(group.title),
            description = newDescription.getOrElse(group.description),
            passwordHash = if (newPassword.nonEmpty) {
              passwordService.hashPassword(newPassword.get)
            } else {
              group.passwordHash
            },
            currencyIsoCode = newCurrencyIsoCode.getOrElse(group.currencyIsoCode),
            created = group.created,
            modified = Timestamp.now()
          )
        )
      }
    } yield groupUid
  }

  private def getMembersToAdd(
    currentMembers: List[GroupMembershipEntity],
    newMemberUids: Option[List[MemberUid]]
  ): IO[DomainError, List[MemberUid]] = {
    if (newMemberUids.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newUids = newMemberUids.getOrElse(List.empty)
    val userUidSet = currentMembers.map(_.memberUid).toSet

    ZIO.succeed(newUids.filter(uid => !userUidSet.contains(uid)).distinct)
  }

  private def getMembersToRemove(
    currentMembers: List[GroupMembershipEntity],
    newMemberUids: Option[List[MemberUid]]
  ): IO[DomainError, List[MembershipUid]] = {
    if (newMemberUids.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newUids = newMemberUids.getOrElse(List.empty).toSet

    ZIO.succeed(
      currentMembers
        .filter(member => !newUids.contains(member.memberUid))
        .map(_.uid)
        .distinct
    )
  }

  private def updateMembers(
    groupUid: GroupUid,
    newMembersOption: Option[List[MemberUid]]
  ): IO[DomainError, List[GroupMembershipEntity]] = {
    if (newMembersOption.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newMembers = newMembersOption.getOrElse(List.empty)

    for {
      _ <- groupMemberDao.removeByGroupUid(groupUid)
      result <- groupMemberDao.add(
        newMembers.map { userUid =>
          GroupMembershipEntity(
            uid = MembershipUid(UUID.randomUUID()),
            groupUid = groupUid,
            memberUid = userUid
          )
        }
      )
    } yield result
  }

  private def isCurrencyIsoCodeValid(
    newCurrencyIsoCode: Option[String]
  ): IO[DomainError, Unit] = {
    defer {
      if (newCurrencyIsoCode.isDefined) {
        validateCurrencyUseCase.isCurrencyIsoCodeValid(newCurrencyIsoCode.get).run
      }

      ()
    }
  }
}
