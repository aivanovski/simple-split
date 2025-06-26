package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.UserEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.github.ai.split.utils.some
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.util.UUID

class UserEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  private val getAllQuery = quote {
    querySchema[UserEntity]("users")
  }

  def getAll(): IO[DomainError, List[UserEntity]] = {
    run(getAllQuery)
      .mapError(_.toDomainError())
  }

  def getByUid(uid: UUID): IO[DomainError, UserEntity] = {
    for {
      users <- getAll()
      user <- ZIO.fromOption(users.find(_.uid == uid))
        .mapError(_ => DomainError(message = s"Failed to find user by uid: $uid".some))
    } yield user
  }

  def getByEmail(email: String): IO[DomainError, UserEntity] = {
    for {
      users <- getAll()
      user <- ZIO.fromOption(users.find(_.email == email))
        .mapError(_ => DomainError(message = s"Failed to find user by email: $email".some))
    } yield user
  }

  def add(user: UserEntity): IO[DomainError, UserEntity] = {
    run(
      quote {
        querySchema[UserEntity]("users")
          .insertValue(lift(user))
      }
    )
      .map(_ => user)
      .mapError(_.toDomainError())
  }

  def getUserUidToUserMap(): IO[DomainError, Map[UUID, UserEntity]] = {
    for {
      users <- getAll()
    } yield {
      users
        .map { user => user.uid -> user }
        .toMap
    }
  }
}
