package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, MemberEntityDao, UserEntityDao}
import com.github.ai.split.data.db.model.{Acknowledgement, GroupUid, MemberEntity, MemberUid, UserUid}
import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.utils.some
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import java.util.UUID

class AddMembersUseCase(
  private val groupRepository: GroupRepository,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: MemberEntityDao,
  private val userDao: UserEntityDao,
  private val validateMemberUseCase: ValidateMemberNameUseCase
) {

  def addMember(
    groupUid: GroupUid,
    name: String
  ): IO[DomainError, MemberEntity] = {
    defer {
      val members = groupRepository.getMembers(groupUid).run

      validateMemberUseCase
        .validateNewMembers(
          currentMemberNames = members.map(_.getName()),
          newMemberNames = List(name)
        )
        .run

      groupMemberDao
        .add(
          MemberEntity(
            uid = MemberUid(UUID.randomUUID()),
            groupUid = groupUid,
            userUid = None,
            name = Some(name),
            email = None,
            acknowledgement = Acknowledgement.NOT_REQUESTED
          )
        )
        .run
    }
  }

  def addMembers(
    groupUid: GroupUid,
    userUids: List[UserUid]
  ): IO[DomainError, List[MemberEntity]] = {
    defer {
      validateUsers(userUids = userUids).run

      canAddMembers(groupUid = groupUid, userUids = userUids).run

      val newMembers = userUids.map { userUid =>
        MemberEntity(
          uid = MemberUid(UUID.randomUUID()),
          groupUid = groupUid,
          userUid = Some(userUid),
          name = None,
          email = None,
          acknowledgement = Acknowledgement.CONFIRMED
        )
      }

      ZIO.collectAll(newMembers.map(newMember => groupMemberDao.add(newMember))).run
    }
  }

  private def validateUsers(
    userUids: List[UserUid]
  ): IO[DomainError, Unit] = {
    defer {
      userDao.getByUids(userUids).run

      ZIO.unit.run
    }
  }

  def canAddMembers(
    groupUid: GroupUid,
    userUids: List[UserUid]
  ): IO[DomainError, Unit] = {
    defer {
      val users = userDao.getByUids(uids = userUids).run
      val members = groupMemberDao.getByGroupUid(groupUid = groupUid).run

      val memberUids = members.flatMap(_.userUid).toSet
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
