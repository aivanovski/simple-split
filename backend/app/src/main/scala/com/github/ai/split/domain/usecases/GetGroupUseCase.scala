package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.{GroupEntityDao, MemberEntityDao}
import com.github.ai.split.data.db.model.{GroupEntity, GroupUid, MemberUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class GetGroupUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: MemberEntityDao
) {

  def getGroupByUid(uid: GroupUid): IO[DomainError, GroupEntity] = groupDao.getByUid(uid)

  def getGroupByMemberUid(
    memberUid: MemberUid
  ): IO[DomainError, GroupEntity] = {
    for {
      member <- groupMemberDao.getByUid(uid = memberUid)
      group <- groupDao.getByUid(uid = member.groupUid)
    } yield group
  }
}
