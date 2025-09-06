package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{GroupUid, UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{some}
import zio.{IO, ZIO}
import zio.direct.*
import slick.jdbc.PostgresProfile.api.*

class UserEntityDao(
  db: AppDatabase,
  private val groupMemberDao: GroupMemberEntityDao
) extends Dao(db = db.context, table = db.UserTable) {

  private val table = db.UserTable

  // TODO: refactor
  def getAll(): IO[DomainError, List[UserEntity]] = {
    queryAll()
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[UserEntity]] = {
    defer {
      val users = groupMemberDao.getByGroupUid(groupUid).run
      val userUids = users.map(_.userUid).toSet
      query(table => table.uid inSet userUids).run
    }
  }

  def findByUid(uid: UserUid): IO[DomainError, Option[UserEntity]] = {
    queryOne(table => table.uid === uid)
  }

  def getByUids(uids: List[UserUid]): IO[DomainError, List[UserEntity]] = {
    val uidSet = uids.toSet

    query(t => t.uid inSet uidSet)
      .flatMap { users =>
        if (users.size == uidSet.size) {
          ZIO.succeed(users)
        } else {
          val foundUids = users.map(_.uid).toSet
          val notFoundUids = uidSet.diff(foundUids).mkString(", ")
          ZIO.fail(DomainError(message = s"Failed to find users: $notFoundUids".some))
        }
      }
  }

  def getByUid(uid: UserUid): IO[DomainError, UserEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find user by uid: $uid".some))
      }
  }

  def add(user: UserEntity): IO[DomainError, UserEntity] = {
    insert(user)
  }

  def update(user: UserEntity): IO[DomainError, UserEntity] = {
    updateOne(
      predicate = { entity => entity.uid === user.uid },
      entity = user
    )
  }

  // TODO: remove function and refactor
  def getUserUidToUserMap(): IO[DomainError, Map[UserUid, UserEntity]] = {
    for {
      users <- getAll()
    } yield {
      users.map { user => user.uid -> user }.toMap
    }
  }
}
