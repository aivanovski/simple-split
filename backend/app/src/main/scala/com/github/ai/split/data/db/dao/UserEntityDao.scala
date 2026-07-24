package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.given
import com.github.ai.split.data.db.model.{UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import slick.jdbc.SQLiteProfile.api.*
import zio.{IO, ZIO}
import zio.direct.*

class UserEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.UserTable) {

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

  def add(user: UserEntity): IO[DomainError, UserEntity] =
    insert(user)

  def delete(uid: UserUid): IO[DomainError, Unit] =
    deleteOne(_.uid === uid)
}
