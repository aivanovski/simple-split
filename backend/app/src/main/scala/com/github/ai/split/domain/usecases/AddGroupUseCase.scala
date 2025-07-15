package com.github.ai.split.domain.usecases

import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.entity.db.GroupEntity
import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.{NewExpense, NewGroup}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}

import java.util.UUID

class AddGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def addGroup(
    newGroup: NewGroup
  ): IO[DomainError, GroupEntity] = {
    // TODO: process members
    for {
      _ <- validateGroupData(newGroup)

      group <- groupDao.add(
        GroupEntity(
          uid = UUID.randomUUID(),
          title = newGroup.title,
          description = newGroup.description,
          passwordHash = if (newGroup.password.nonEmpty) {
            passwordService.hashPassword(newGroup.password).some
          } else {
            None
          }
        )
      )
    } yield group
  }

  private def validateGroupData(data: NewGroup): IO[DomainError, Unit] = {
    if (data.password.isBlank) {
      return ZIO.fail(DomainError(message = "Specified password is empty".some))
    }

    if (data.password.trim.length < 4) {
      return ZIO.fail(DomainError(message = "Specified password is too weak".some))
    }

    if (data.title.isBlank) {
      return ZIO.fail(DomainError(message = "Group title is empty".some))
    }

    ZIO.succeed(())
  }
}
