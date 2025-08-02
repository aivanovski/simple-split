package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.{GroupMemberEntity, GroupUid, MemberUid, UserUid}
import com.github.ai.split.entity.db.UserUid._
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{some, toDomainError}
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.sql.SQLException

class GroupMemberEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  // TODO: refactor
  def getAll(): IO[DomainError, List[GroupMemberEntity]] = {
    val query = quote {
      querySchema[GroupMemberEntity]("group_members")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, List[GroupMemberEntity]] = {
    val query = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(_.groupUid == lift(groupUid))
    }

    run(query).mapError(_.toDomainError())
  }

  def getByUid(uid: MemberUid): IO[DomainError, GroupMemberEntity] = {
    val query = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(_.uid == lift(uid))
    }

    for {
      members <- run(query).mapError(_.toDomainError())
      member <- ZIO.fromOption(members.headOption).mapError { _ =>
        DomainError(message = s"Failed to find member by uid: $uid".some)
      }
    } yield member
  }

  def getByUserUid(userUid: UserUid): IO[DomainError, GroupMemberEntity] = {
    val query = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(_.userUid == lift(userUid))
    }

    for {
      members <- run(query).mapError(_.toDomainError())
      member <- ZIO.fromOption(members.headOption).mapError { _ =>
        DomainError(message = s"Failed to find member by user uid: $userUid".some)
      }
    } yield member
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

  def removeByGroupUid(groupUid: GroupUid): IO[DomainError, Unit] = {
    val deleteQuery = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(_.groupUid == lift(groupUid))
        .delete
    }

    run(deleteQuery)
      .map(_ => ())
      .mapError(_.toDomainError())
  }

  def removeByUid(
    uid: MemberUid
  ): IO[DomainError, Unit] = {
    val deleteQuery = quote {
      querySchema[GroupMemberEntity]("group_members")
        .filter(member => member.uid == lift(uid))
        .delete
    }

    run(deleteQuery)
      .map(_ => ())
      .mapError(_.toDomainError())
  }
}
