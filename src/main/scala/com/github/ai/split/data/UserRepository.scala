package com.github.ai.split.data

import com.github.ai.split.data.UserRepository.{ADMIN, DONALD, GOOFY, MICKEY}
import com.github.ai.split.entity.User
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some

import java.util.UUID
import scala.collection.mutable
import zio._

class UserRepository {

  private val users = mutable.HashMap[UUID, User](
    ADMIN.uid -> ADMIN,
    GOOFY.uid -> GOOFY,
    MICKEY.uid -> MICKEY,
    DONALD.uid -> DONALD
  )

  def getUsers(): IO[DomainError, List[User]] = {
    ZIO.succeed(users.values.toList)
  }

  def getByUid(uid: UUID): IO[DomainError, User] = {
    ZIO.fromOption(users.get(uid))
      .mapError(_ => new DomainError(message = s"Failed to find User".some))
  }

  def getByEmail(email: String): IO[DomainError, User] = {
    ZIO.fromOption(users.values.toList.find { user => user.email.equalsIgnoreCase(email) })
      .mapError { _ => new DomainError(message = s"Failed to find user by email: $email".some) }
  }

  def add(user: User): IO[DomainError, User] = {
    val uid = UUID.randomUUID()

    users.put(uid, user.copy(uid = uid))

    ZIO.fromOption(users.get(uid))
      .mapError(_ => new DomainError(message = s"Failed to find User".some))
  }

  def getUserUidToUserMap(): IO[DomainError, Map[UUID, User]] = {
    getUsers().map { users =>
      users.map { user => user.uid -> user }
        .toMap
    }
  }
}

object UserRepository {
  val ADMIN = User(new UUID(1, 1), "admin@mail.com", "abc123")

  val MICKEY = User(new UUID(2, 1), "mickey@mail.com", "abc123")
  val GOOFY = User(new UUID(2, 2), "goofy@mail.com", "abc123")
  val DONALD = User(new UUID(2, 3), "donald@mail.com", "abc123")
}