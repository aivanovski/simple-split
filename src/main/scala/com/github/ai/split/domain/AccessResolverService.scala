package com.github.ai.split.domain

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.utils.some
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}

import java.util.UUID

class AccessResolverService(
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def canModifyGroup(
    userUid: UUID,
    groupUid: UUID
  ): IO[DomainError, Unit] = {
    for {
      group <- groupDao.getByUid(groupUid)
      members <- groupMemberDao.getByGroupUid(groupUid)
      _ <- if (group.ownerUid == userUid || members.exists(_.userUid == userUid)) {
        ZIO.succeed(())
      } else {
        ZIO.fail(DomainError(message = "Unable to access the group".some))
      }
    } yield ()
  }
}
