package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.model.{GroupMembershipEntity, GroupUid, MemberUid, MembershipUid}
import com.github.ai.split.data.db.given
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}
import slick.jdbc.SQLiteProfile.api.*

class GroupMembershipEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.GroupMembershipTable) {

  // TODO: refactor
  def getAll(): IO[DomainError, List[GroupMembershipEntity]] = {
    queryAll()
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[GroupMembershipEntity]] = {
    query(table => table.groupUid === groupUid)
  }

  def getByUid(uid: MembershipUid): IO[DomainError, GroupMembershipEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find member by uid: $uid".some))
      }
  }

  def getByMemberUid(userUid: MemberUid): IO[DomainError, GroupMembershipEntity] = {
    queryOne(table => table.memberUid === userUid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find member by user uid: $userUid".some))
      }
  }

  def add(member: GroupMembershipEntity): IO[DomainError, GroupMembershipEntity] = {
    insert(member)
  }

  def add(members: List[GroupMembershipEntity]): IO[DomainError, List[GroupMembershipEntity]] = {
    insertAll(members)
  }

  def removeByGroupUid(groupUid: GroupUid): IO[DomainError, Unit] = {
    delete(table => table.groupUid === groupUid)
  }

  def removeByUid(
    uid: MembershipUid
  ): IO[DomainError, Unit] = {
    deleteOne(table => table.uid === uid)
  }
}
