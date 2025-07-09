package com.github.ai.split.domain

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.utils.some
import com.github.ai.split.entity.exception.DomainError
import org.mindrot.jbcrypt.BCrypt
import zio.{IO, ZIO}

import java.util.UUID

class AccessResolverService(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
) {

  def canAccessToGroups(
    groupUids: List[UUID],
    passwords: List[String]
  ): IO[DomainError, Unit] =
    ZIO.collectAll(
        groupUids.zip(passwords)
          .map((groupUid, password) => canAccessToGroup(groupUid, password))
      )
      .map(_ => ())

  def canAccessToGroup(
    groupUid: UUID,
    password: String
  ): IO[DomainError, Unit] = {
    for {
      group <- groupDao.getByUid(groupUid)
      _ <- isPasswordMatch(password = password, passwordHash = group.passwordHash)
    } yield ()
  }

  private def isPasswordMatch(
    password: String,
    passwordHash: Option[String]
  ): IO[DomainError, Unit] = {
    if (password.isEmpty && passwordHash == None) {
      ZIO.succeed(())
    } else {
      passwordService.verifyPassword(
        password = password,
        hashedPassword = passwordHash.getOrElse("")
      )
    }
  }
}
