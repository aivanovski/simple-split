package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.model.{GroupEntity, GroupUid}
import com.github.ai.split.data.db.given
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import slick.jdbc.SQLiteProfile.api.*
import zio.direct.{defer, run}
import zio.{IO, ZIO}

class GroupEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.GroupTable) {

  def getByUids(uids: List[GroupUid]): IO[DomainError, List[GroupEntity]] = defer {
    val uidSet = uids.toSet
    val groups = query(t => t.uid inSet uidSet).run

    if (groups.size == uids.size) {
      groups
    } else {
      ZIO.fail(DomainError(message = s"Failed to find requested entities by uids: $uids".some)).run
    }
  }

  def getByUid(uid: GroupUid): IO[DomainError, GroupEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { groupOption =>
        ZIO
          .fromOption(groupOption)
          .mapError(_ => DomainError(message = s"Failed to find group by uid: $uid".some))
      }
  }

  def findByUid(uid: GroupUid): IO[DomainError, Option[GroupEntity]] = {
    queryOne(table => table.uid === uid)
  }

  def add(group: GroupEntity): IO[DomainError, GroupEntity] = {
    insert(group)
  }

  def update(group: GroupEntity): IO[DomainError, GroupEntity] = {
    updateOne(table => table.uid === group.uid, entity = group)
  }
}
