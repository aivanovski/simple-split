package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.usecases.AddMemberUseCase
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.db.{GroupEntity, GroupMemberEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*

import java.util.UUID

class UpdateGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val userDao: UserEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
  private val addMemberUseCase: AddMemberUseCase
) {

  def updateGroup(
    groupUid: UUID,
    newPassword: Option[String],
    newTitle: Option[String],
    newDescription: Option[String],
    newMemberUids: Option[List[UUID]]
  ): IO[DomainError, UUID] = {
    for {
      group <- groupDao.getByUid(uid = groupUid)
      currentMemberUids <- groupMemberDao.getByGroupUid(groupUid = groupUid)
        .map(members => members.map(_.userUid))

      membersToAdd <- getMembersToAdd(
        memberUids = currentMemberUids,
        newMemberUids = newMemberUids
      )
      _ <- canAddMembers(groupUid = groupUid, toAdd = membersToAdd)

      membersToRemove <- getMembersToRemove(
        memberUids = currentMemberUids,
        newMemberUids = newMemberUids
      )
      _ <- canRemoveMembers(groupUid = groupUid, toRemove = membersToRemove)

      _ <- updateMembers(groupUid = groupUid, newMembersOption = newMemberUids)

      _ <- groupDao.update(
        GroupEntity(
          uid = groupUid,
          title = newTitle.getOrElse(group.title),
          description = newDescription.getOrElse(group.description),
          passwordHash = if (newPassword.isDefined) {
            Some(passwordService.hashPassword(newPassword.get))
          } else {
            group.passwordHash
          }
        )
      )
    } yield groupUid
  }

  private def getMembersToAdd(
    memberUids: List[UUID],
    newMemberUids: Option[List[UUID]]
  ): IO[DomainError, List[UUID]] = {
    if (newMemberUids.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newUids = newMemberUids.getOrElse(List.empty)
    val memberUidsSet = memberUids.toSet

    ZIO.succeed(newUids.filter(uid => !memberUidsSet.contains(uid)).distinct)
  }

  private def getMembersToRemove(
    memberUids: List[UUID],
    newMemberUids: Option[List[UUID]]
  ): IO[DomainError, List[UUID]] = {
    if (newMemberUids.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newUids = newMemberUids.getOrElse(List.empty).toSet

    ZIO.succeed(memberUids.filter(uid => !newUids.contains(uid)).distinct)
  }

  private def updateMembers(
    groupUid: UUID,
    newMembersOption: Option[List[UUID]]
  ): IO[DomainError, List[GroupMemberEntity]] = {
    if (newMembersOption.isEmpty) {
      return ZIO.succeed(List.empty)
    }

    val newMembers = newMembersOption.getOrElse(List.empty)

    for {
      _ <- groupMemberDao.removeByGroupUid(groupUid)
      result <- groupMemberDao.add(
        newMembers.map { userUid =>
          GroupMemberEntity(
            groupUid = groupUid,
            userUid = userUid
          )
        }
      )
    } yield result
  }

  private def canAddMembers(
    groupUid: UUID,
    toAdd: List[UUID]
  ): IO[DomainError, Unit] = {
    if (toAdd.isEmpty) {
      return ZIO.succeed(())
    }

    for {
      users <- ZIO.collectAll(toAdd.map(uid => userDao.getByUid(uid)))
    } yield ()
  }

  private def canRemoveMembers(
    groupUid: UUID,
    toRemove: List[UUID]
  ): IO[DomainError, Unit] = {
    if (toRemove.isEmpty) {
      return ZIO.succeed(())
    }

    val toRemoveSet = toRemove.toSet

    for {
      members <- groupMemberDao.getByGroupUid(groupUid)
      _ <- {
        if (members.size - toRemove.size < 2) {
          ZIO.fail(DomainError(message = "Can't make less then 2 members".some))
        } else {
          ZIO.succeed(())
        }
      }

      allPaidBy <- paidByDao.getByGroupUid(groupUid)
      _ <- {
        val paidByMembers = allPaidBy
          .filter(payer => toRemoveSet.contains(payer.userUid))
          .map(payer => payer.userUid)

        if (paidByMembers.nonEmpty) {
          val uids = paidByMembers.mkString(", ")
          ZIO.fail(DomainError(message = s"Failed to remove users as they mentioned as payers: $uids".some))
        } else {
          ZIO.succeed(())
        }
      }

      allSplits <- splitBetweenDao.getByGroupUid(groupUid)
      _ <- {
        val splits = allSplits
          .filter(split => toRemoveSet.contains(split.userUid))
          .map(split => split.userUid)

        if (splits.nonEmpty) {
          val uids = splits.mkString(", ")
          ZIO.fail(DomainError(message = s"Failed to remove users as they mentioned as payers: $uids".some))
        } else {
          ZIO.succeed(())
        }
      }
    } yield ()
  }
}
