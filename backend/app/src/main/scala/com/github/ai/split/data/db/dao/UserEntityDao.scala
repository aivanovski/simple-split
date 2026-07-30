package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.given
import com.github.ai.split.data.db.model.{MemberEntity, UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import slick.jdbc.SQLiteProfile.api.*
import zio.{IO, ZIO}
import zio.direct.*

class UserEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.UserTable) {

  // TODO: refactor
  def getAll(): IO[DomainError, List[UserEntity]] =
    queryAll()

  def getByUid(uid: UserUid): IO[DomainError, UserEntity] =
    defer {
      findByUid(uid).run match
        case Some(user) => user
        case None =>
          ZIO
            .fail(DomainError(message = s"Failed to find user by uid: $uid".some))
            .run
    }

  def findByUid(uid: UserUid): IO[DomainError, Option[UserEntity]] =
    queryOne(_.uid === uid)

  def findByEmail(email: String): IO[DomainError, Option[UserEntity]] =
    queryOne(_.email === email)

  def getByUids(uids: List[UserUid]): IO[DomainError, List[UserEntity]] = defer {
    val uidSet = uids.toSet
    val users = query(t => t.uid inSet uidSet).run

    if (users.size == uids.size) {
      users
    } else {
      ZIO.fail(DomainError(message = s"Failed to find requested entities by uids: $uids".some)).run
    }
  }

  def add(user: UserEntity): IO[DomainError, UserEntity] =
    insert(user)

  def delete(uid: UserUid): IO[DomainError, Unit] =
    deleteOne(_.uid === uid)
}
