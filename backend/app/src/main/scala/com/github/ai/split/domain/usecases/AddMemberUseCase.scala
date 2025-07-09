package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.entity.db.GroupMemberEntity
import com.github.ai.split.utils.some
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddMemberUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val userDao: UserEntityDao
) {

  def addMember(
    groupUid: UUID,
    userUid: UUID
  ): IO[DomainError, GroupMemberEntity] = {
    for {
      members <- groupMemberDao.getByGroupUid(groupUid)
      newMember <- {
        if (members.exists(_.userUid == userUid)) {
          ZIO.fail(new DomainError(message = "Member is already added".some))
        } else {
          val newMember = GroupMemberEntity(
            groupUid = groupUid,
            userUid = userUid
          )

          groupMemberDao.add(newMember)
        }
      }
    } yield newMember
  }
}
