package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.api.request.PostGroupRequest
import com.github.ai.split.entity.db.GroupEntity
import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.entity.exception.DomainError
import zio.IO

import java.util.UUID

class AddGroupUseCase(
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def addGroup(
    entity: GroupEntity
  ): IO[DomainError, GroupEntity] = {
    // TODO: process members
    for {
      newGroup <- groupDao.add(entity)
      //      _ <- groupMemberDao.add(members)
    } yield newGroup
  }
}
