package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.model.{GroupUid, MemberEntity, MemberUid, UserUid}
import com.github.ai.split.data.db.given
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}
import zio.direct.*
import slick.jdbc.SQLiteProfile.api.*

class MemberEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.MemberTable) {

  private val table = db.MemberTable

  // TODO: refactor
  def getAll(): IO[DomainError, List[MemberEntity]] = {
    queryAll()
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[MemberEntity]] = {
    defer {
      query(table => table.groupUid === groupUid).run
    }
  }

  def findByUid(uid: MemberUid): IO[DomainError, Option[MemberEntity]] = {
    queryOne(table => table.uid === uid)
  }

  def getByUids(uids: List[MemberUid]): IO[DomainError, List[MemberEntity]] = defer {
    val uidSet = uids.toSet
    val members = query(t => t.uid inSet uidSet).run

    if (members.size == uids.size) {
      members
    } else {
      ZIO.fail(DomainError(message = s"Failed to find requested entities by uids: $uids".some)).run
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

  def getByUserUid(userUid: UserUid): IO[DomainError, List[MemberEntity]] =
    getAll().map(_.filter(_.userUid.contains(userUid)))

  def removeByGroupUid(groupUid: GroupUid): IO[DomainError, Unit] =
    delete(table => table.groupUid === groupUid)

  def removeByUid(uid: MemberUid): IO[DomainError, Unit] =
    deleteOne(table => table.uid === uid)
}
