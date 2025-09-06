package com.github.ai.split.data.db.dao

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.data.db.{given}
import com.github.ai.split.entity.db.{GroupMemberEntity, GroupUid, MemberUid, UserUid}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.{IO, ZIO}
import slick.jdbc.PostgresProfile.api.*

class GroupMemberEntityDao(
  db: AppDatabase
) extends Dao(db = db.context, table = db.GroupMemberTable) {

  // TODO: refactor
  def getAll(): IO[DomainError, List[GroupMemberEntity]] = {
    queryAll()
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[GroupMemberEntity]] = {
    query(table => table.groupUid === groupUid)
  }

  def getByUid(uid: MemberUid): IO[DomainError, GroupMemberEntity] = {
    queryOne(table => table.uid === uid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find member by uid: $uid".some))
      }
  }

  def getByUserUid(userUid: UserUid): IO[DomainError, GroupMemberEntity] = {
    queryOne(table => table.userUid === userUid)
      .flatMap { option =>
        ZIO
          .fromOption(option)
          .mapError(_ => DomainError(message = s"Failed to find member by user uid: $userUid".some))
      }
  }

  def add(member: GroupMemberEntity): IO[DomainError, GroupMemberEntity] = {
    insert(member)
  }

  def add(members: List[GroupMemberEntity]): IO[DomainError, List[GroupMemberEntity]] = {
    insertAll(members)
  }

  def removeByGroupUid(groupUid: GroupUid): IO[DomainError, Unit] = {
    delete(table => table.groupUid === groupUid)
  }

  def removeByUid(
    uid: MemberUid
  ): IO[DomainError, Unit] = {
    deleteOne(table => table.uid === uid)
  }
}
