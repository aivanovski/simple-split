package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.given
import com.github.ai.split.entity.db.{GroupUid, MemberEntity, MemberUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}
import zio.direct.*
import slick.jdbc.SQLiteProfile.api.*

class MemberEntityDao(
  db: AppDatabase,
  private val groupMemberDao: GroupMembershipEntityDao
) extends Dao(db = db.context, table = db.MemberTable) {

  private val table = db.MemberTable

  // TODO: refactor
  def getAll(): IO[DomainError, List[MemberEntity]] = {
    queryAll()
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[MemberEntity]] = {
    defer {
      val users = groupMemberDao.getByGroupUid(groupUid).run
      val memberUids = users.map(_.memberUid).toSet
      query(table => table.uid inSet memberUids).run
    }
  }

  def findByUid(uid: MemberUid): IO[DomainError, Option[MemberEntity]] = {
    queryOne(table => table.uid === uid)
  }

  def getByUids(uids: List[MemberUid]): IO[DomainError, List[MemberEntity]] = {
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

  def getByUid(uid: MemberUid): IO[DomainError, MemberEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find user by uid: $uid".some))
      }
  }

  def add(user: MemberEntity): IO[DomainError, MemberEntity] = {
    insert(user)
  }

  def update(user: MemberEntity): IO[DomainError, MemberEntity] = {
    updateOne(
      predicate = { entity => entity.uid === user.uid },
      entity = user
    )
  }

  // TODO: remove function and refactor
  def getMemberUidToMemberMap(): IO[DomainError, Map[MemberUid, MemberEntity]] = {
    for {
      users <- getAll()
    } yield {
      users.map { user => user.uid -> user }.toMap
    }
  }
}
