package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, MemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.data.db.model.{GroupEntity, GroupUid, MemberEntity, MemberUid, Timestamp}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class UpdateGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val memberDao: MemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
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
      currentMembers <- memberDao.getByGroupUid(groupUid = groupUid)

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

  private def getMembersToRemove(
    currentMembers: List[MemberEntity],
    newMemberUids: Option[List[MemberUid]]
  ): IO[DomainError, List[MemberUid]] = {
    if (newMemberUids.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newUids = newMemberUids.getOrElse(List.empty).toSet

    ZIO.succeed(
      currentMembers
        .filter(member => !newUids.contains(member.uid))
        .map(_.uid)
        .distinct
    )
  }

  private def updateMembers(
    groupUid: GroupUid,
    newMembersOption: Option[List[MemberUid]]
  ): IO[DomainError, List[MemberEntity]] = {
    if (newMembersOption.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    for {
      members <- memberDao.getByGroupUid(groupUid)
      retained = members.filter(member => newMembersOption.get.contains(member.uid))
      removed = members.filterNot(member => retained.exists(_.uid == member.uid))
      _ <- ZIO.foreachDiscard(removed)(member => memberDao.removeByUid(member.uid))
    } yield retained
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
