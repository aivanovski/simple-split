package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao}
import com.github.ai.split.entity.db.{GroupUid, MemberUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

import java.util.UUID

class RemoveMembersUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val paidByDao: PaidByEntityDao,
  private val splitBetweenDao: SplitBetweenEntityDao,
) {

  def removeMemberByUids(
    memberUids: List[MemberUid]
  ): IO[DomainError, Unit] = {
    defer {
      val groupUid = getGroupUid(memberUids).run

      validateAllInTheSameGroup(groupUid = groupUid, memberUids = memberUids).run

      canRemoveMembers(groupUid = groupUid, memberUids = memberUids).run

      for (memberUid <- memberUids) {
        groupMemberDao.removeByUid(memberUid).run
      }
    }
  }

  private def getGroupUid(memberUids: List[MemberUid]): IO[DomainError, GroupUid] = {
    defer {
      if (memberUids.isEmpty) {
        ZIO.fail(DomainError(message = "At least one member is required".some)).run
      }

      val member = groupMemberDao.getByUid(uid = memberUids.head).run

      member.groupUid
    }
  }

  private def validateAllInTheSameGroup(
    groupUid: GroupUid,
    memberUids: List[MemberUid]
  ): IO[DomainError, Unit] = {
    defer {
      val members = groupMemberDao.getByGroupUid(groupUid = groupUid).run

      val groupMemberUids = members
        .map(member => member.uid)
        .toSet

      val containsAll = memberUids.forall(uid => groupMemberUids.contains(uid))
      if (containsAll) {
        ZIO.unit.run
      } else {
        ZIO.fail(DomainError(message = "Invalid group member specified".some)).run
      }
    }
  }

  def canRemoveMembers(
    groupUid: GroupUid,
    memberUids: List[MemberUid]
  ): IO[DomainError, Unit] = {
    val userUidSet = memberUids.toSet

    for {
      members <- groupMemberDao.getByGroupUid(groupUid)
      _ <- {
        if (members.size - memberUids.size < 2) {
          ZIO.fail(DomainError(message = "Can't make less then 2 members".some))
        } else {
          ZIO.succeed(())
        }
      }

      allPaidBy <- paidByDao.getByGroupUid(groupUid)
      _ <- {
        val paidByMembers = allPaidBy
          .filter(payer => userUidSet.contains(payer.memberUid))
          .map(payer => payer.memberUid)

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
          .filter(split => userUidSet.contains(split.memberUid))
          .map(split => split.memberUid)

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
