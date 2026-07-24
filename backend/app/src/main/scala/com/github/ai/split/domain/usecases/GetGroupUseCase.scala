package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMembershipEntityDao}
import com.github.ai.split.entity.db.{GroupEntity, GroupUid, MembershipUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class GetGroupUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMembershipEntityDao
) {

  def getGroupByUid(uid: GroupUid): IO[DomainError, GroupEntity] = groupDao.getByUid(uid)

  def getGroupByMemberUid(
    memberUid: MembershipUid
  ): IO[DomainError, GroupEntity] = {
    for {
      member <- groupMemberDao.getByUid(uid = memberUid)
      group <- groupDao.getByUid(uid = member.groupUid)
    } yield group
  }
}
