package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.{GroupMemberEntity, UserEntity}
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

  def getAll(): IO[DomainError, List[UserEntity]] = {
    val query = quote {
      querySchema[UserEntity]("users")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[UserEntity]] = {
    val query = quote {
      for {
        member <- querySchema[GroupMemberEntity]("group_members")
          .filter(_.groupUid == lift(groupUid))
        u <- querySchema[UserEntity]("users") if member.userUid == u.uid
      } yield (member, u)
    }

    for {
      members <- run(query).mapError(_.toDomainError())
    } yield members.map((member, user) => user)
  }

  def getByUid(uid: UUID): IO[DomainError, UserEntity] = {
    val query = quote {
      querySchema[UserEntity]("users")
        .filter(_.uid == lift(uid))
    }

    for {
      users <- run(query).mapError(_.toDomainError())
      user <- if (users.nonEmpty) {
        ZIO.succeed(users.head)
      } else {
        ZIO.fail(DomainError(message = s"Failed to find user by uid: $uid".some))
      }
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

object UserEntityDao {
  val TableName = "users"
}
