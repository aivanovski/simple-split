package com.github.ai.split.domain.usecases

import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.entity.db.GroupEntity
import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.NewGroup
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.IO

import java.util.UUID

class AddGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def addGroup(
    group: NewGroup
  ): IO[DomainError, GroupEntity] = {
    // TODO: process members
    for {
      newGroup <- groupDao.add(
        GroupEntity(
          uid = UUID.randomUUID(),
          title = group.title,
          description = group.description,
          passwordHash = if (group.password.nonEmpty) {
            passwordService.hashPassword(group.password).some
          } else {
            None
          }
        )
      )
    } yield newGroup
  }
}
