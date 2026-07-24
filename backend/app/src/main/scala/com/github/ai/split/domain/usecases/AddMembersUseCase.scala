package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMembershipEntityDao, MemberEntityDao}
import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.db.{GroupMembershipEntity, GroupUid, MembershipUid, MemberEntity, MemberUid}
import com.github.ai.split.utils.some
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import java.util.UUID

class AddMembersUseCase(
  private val groupRepository: GroupRepository,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMembershipEntityDao,
  private val userDao: MemberEntityDao,
  private val validateMemberUseCase: ValidateMemberNameUseCase
) {

  def addMember(
    groupUid: GroupUid,
    name: String
  ): IO[DomainError, GroupMembershipEntity] = {
    defer {
      val members = groupRepository.getMembers(groupUid).run

      validateMemberUseCase
        .validateNewMembers(
          currentMemberNames = members.map(_.user.name),
          newMemberNames = List(name)
        )
        .run

      val user = userDao
        .add(
          MemberEntity(
            uid = MemberUid(UUID.randomUUID()),
            name = name
          )
        )
        .run

      groupMemberDao
        .add(
          GroupMembershipEntity(
            uid = MembershipUid(UUID.randomUUID()),
            groupUid = groupUid,
            memberUid = user.uid
          )
        )
        .run
    }
  }

  def addMembers(
    groupUid: GroupUid,
    userUids: List[MemberUid]
  ): IO[DomainError, List[GroupMembershipEntity]] = {
    defer {
      validateUsers(userUids = userUids).run

      canAddMembers(groupUid = groupUid, userUids = userUids).run

      val newMembers = userUids.map { userUid =>
        GroupMembershipEntity(
          uid = MembershipUid(UUID.randomUUID()),
          groupUid = groupUid,
          memberUid = userUid
        )
      }

      ZIO.collectAll(newMembers.map(newMember => groupMemberDao.add(newMember))).run
    }
  }

  private def validateUsers(
    userUids: List[MemberUid]
  ): IO[DomainError, Unit] = {
    defer {
      userDao.getByUids(userUids).run

      ZIO.unit.run
    }
  }

  def canAddMembers(
    groupUid: GroupUid,
    userUids: List[MemberUid]
  ): IO[DomainError, Unit] = {
    defer {
      val users = userDao.getByUids(uids = userUids).run
      val members = groupMemberDao.getByGroupUid(groupUid = groupUid).run

      val memberUids = members.map(_.memberUid).toSet
      val addedUsers = users.filter(user => memberUids.contains(user.uid))
      if (addedUsers.nonEmpty) {
        val addedUids = addedUsers.map(_.uid).mkString(", ")
        ZIO.fail(DomainError(message = s"Users already added: $addedUids".some)).run
      }

      val userNames = users.map(_.name)
      validateMemberUseCase
        .validateNewMembers(
          groupUid = groupUid,
          newMemberNames = userNames
        )
        .run
    }
  }
}
