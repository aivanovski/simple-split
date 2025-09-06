package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{GroupEntity, GroupUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import slick.jdbc.PostgresProfile.api.*
import zio.{IO, ZIO}

class GroupEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.GroupTable) {

  def getByUids(uids: List[GroupUid]): IO[DomainError, List[GroupEntity]] = {
    val uidSet = uids.toSet
    query(table => table.uid inSet uidSet)
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
