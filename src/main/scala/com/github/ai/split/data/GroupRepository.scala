package com.github.ai.split.data

import com.github.ai.split.data.GroupRepository.DISNEY_LAND_TRIP
import com.github.ai.split.data.UserRepository.{DONALD, GOOFY, MICKEY}
import com.github.ai.split.entity.Group
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import zio.*

import java.util.UUID
import scala.collection.mutable

class GroupRepository {

  private val groups = mutable.HashMap[UUID, Group](
    DISNEY_LAND_TRIP.uid -> DISNEY_LAND_TRIP
  )

  def getGroups(): IO[DomainError, List[Group]] = {
    ZIO.succeed(groups.values.toList)
  }

  def getByUid(uid: UUID): IO[DomainError, Group] = {
    ZIO.fromOption(groups.get(uid))
      .mapError(_ => new DomainError(message = s"Failed to find Group".some))
  }

  def add(group: Group): IO[DomainError, Group] = {
    val uid = UUID.randomUUID()

    groups.put(uid, group.copy(uid = uid))

    ZIO.fromOption(groups.get(uid))
      .mapError(_ => new DomainError(message = s"Failed to find User".some))
  }

  def updateGroup(group: Group): IO[DomainError, Group] = {
    if (group.uid.isEmpty()) {
      return ZIO.fail(new DomainError(message = "Group UID is empty".some))
    }

    groups.put(group.uid, group)

    getByUid(group.uid)
  }
}

object GroupRepository {
  val DISNEY_LAND_TRIP = Group(
    uid = new UUID(255, 1),
    ownerUid = UserRepository.ADMIN.uid,
    title = "Disney Land Trip",
    description = "The place where dreams come true",
    members = List(
      GOOFY.uid,
      DONALD.uid,
      MICKEY.uid
    ),
    expenses = List.empty
  )
}