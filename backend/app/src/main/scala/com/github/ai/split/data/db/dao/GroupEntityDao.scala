package com.github.ai.split.data.db.dao

import com.github.ai.split.entity.db.GroupEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.github.ai.split.utils.some
import io.getquill.jdbczio.Quill
import io.getquill.generic.*
import io.getquill.*
import zio.*

import java.sql.SQLException
import java.util.UUID

class GroupEntityDao(
  quill: Quill.H2[SnakeCase]
) {

  import quill._

  def getAll(): IO[DomainError, List[GroupEntity]] = {
    val query = quote {
      querySchema[GroupEntity]("groups")
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByUids(uids: List[UUID]): IO[DomainError, List[GroupEntity]] = {
    val uidSet = uids.toSet
    
    val query = quote {
      querySchema[GroupEntity]("groups")
        .filter(gr => liftQuery(uidSet).contains(gr.uid))
    }

    run(query)
      .mapError(_.toDomainError())
  }

  def getByUid(uid: UUID): IO[DomainError, GroupEntity] = {
    for {
      groups <- getAll()
      group <- ZIO.fromOption(groups.find(_.uid == uid))
        .mapError(_ => DomainError(message = s"Failed to find group by uid: $uid".some))
    } yield group
  }

  def add(group: GroupEntity): IO[DomainError, GroupEntity] = {
    run(
      quote {
        querySchema[GroupEntity]("groups")
          .insertValue(lift(group))
      }
    )
      .map(_ => group)
      .mapError(_.toDomainError())
  }

  def update(group: GroupEntity): IO[DomainError, GroupEntity] = {
    val updateQuery = quote {
      querySchema[GroupEntity]("groups")
        .filter(_.uid == lift(group.uid))
        .updateValue(lift(group))
    }

    run(updateQuery)
      .map(_ => group)
      .mapError(_.toDomainError())
  }
}
