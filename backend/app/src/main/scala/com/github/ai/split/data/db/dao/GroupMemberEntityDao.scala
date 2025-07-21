package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.GroupMemberEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.util.UUID
import java.sql.SQLException

class GroupMemberEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[GroupMemberEntity]] = {
    val query = quote {
      querySchema[GroupMemberEntity]("group_members")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: UUID): IO[DomainError, List[GroupMemberEntity]] = {
    for {
      allMembers <- getAll()
      groupMembers <- ZIO.succeed(allMembers.filter(_.groupUid == groupUid))
    } yield groupMembers
  }

  def add(member: GroupMemberEntity): IO[DomainError, GroupMemberEntity] = {
    run(
      quote {
        querySchema[GroupMemberEntity]("group_members")
          .insertValue(lift(member))
      }
    )
      .map(_ => member)
      .mapError(_.toDomainError())
  }

  def add(members: List[GroupMemberEntity]): IO[DomainError, List[GroupMemberEntity]] = {
    val insertQuery = quote {
      liftQuery(members).foreach { member =>
        querySchema[GroupMemberEntity]("group_members")
          .insertValue(member)
      }
    }

    val result: IO[SQLException, List[Long]] = run(insertQuery)

    result
      .map(_ => members)
      .mapError(_.toDomainError())
  }

  def removeByGroupUid(groupUid: UUID): IO[DomainError, Unit] = {
    val deleteQuery = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(_.groupUid == lift(groupUid))
        .delete
    }

    run(deleteQuery)
      .map(_ => ())
      .mapError(_.toDomainError())
  }
}