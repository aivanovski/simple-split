package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

class ValidateMemberNameUseCase(
  private val groupRepository: GroupRepository
) {

  def validateNewMembers(
    groupUid: GroupUid,
    newMemberNames: List[String]
  ): IO[DomainError, Unit] = {
    defer {
      val members = groupRepository.getMembers(groupUid).run

      validateNewMembers(
        currentMemberNames = members.map(_.user.name),
        newMemberNames = newMemberNames
      ).run
    }
  }

  def validateNewMembers(
    currentMemberNames: List[String],
    newMemberNames: List[String]
  ): IO[DomainError, Unit] = {
    if (newMemberNames.isEmpty) {
      return ZIO.fail(DomainError(message = "No new members specified".some))
    }

    val invalidNames = newMemberNames.filter(name => name.isBlank)
    if (invalidNames.nonEmpty) {
      return ZIO.fail(
        DomainError(
          message = s"Invalid member names: ${invalidNames.mkString(", ")}".some
        )
      )
    }

    val repeatingNames = newMemberNames.diff(newMemberNames.distinct)
    if (repeatingNames.nonEmpty) {
      return ZIO.fail(
        DomainError(
          message = s"Member names are not unique: ${repeatingNames.mkString(", ")}".some
        )
      )
    }

    val currentNames = currentMemberNames.toSet
    val existingNames = newMemberNames.filter(newName => currentNames.contains(newName))
    if (existingNames.nonEmpty) {
      return ZIO.fail(
        DomainError(
          message = s"Members already exists: ${existingNames.mkString(", ")}".some
        )
      )
    }

    ZIO.unit
  }
}
