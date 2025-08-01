package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.db.{GroupUid, MemberUid}
import com.github.ai.split.entity.{Member, UserReference, MemberReference, NameReference}
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

class ResolveUserReferencesUseCase(
  private val groupRepository: GroupRepository
) {

  def validateReferences(
    allMembers: List[Member],
    references: List[UserReference]
  ): IO[DomainError, Unit] = {
    for {
      _ <- resolveReferences(allMembers, references)
    } yield ()
  }

  def resolveReferences(
    allMembers: List[Member],
    references: List[UserReference]
  ): IO[DomainError, List[Member]] = {
    defer {
      val memberUidToMemberMap = allMembers.map(member => member.entity.uid -> member).toMap
      val memberNameToMemberMap = allMembers.map(member => member.user.name -> member).toMap

      resolveReferences(
        references = references,
        memberUidToMemberMap = memberUidToMemberMap,
        memberNameToMemberMap = memberNameToMemberMap
      ).run
    }
  }

  private def resolveReferences(
    references: List[UserReference],
    memberUidToMemberMap: Map[MemberUid, Member],
    memberNameToMemberMap: Map[String, Member]
  ): IO[DomainError, List[Member]] = {
    ZIO.collectAll(
      references.map { reference =>
        resolveUserReference(reference, memberUidToMemberMap, memberNameToMemberMap)
      }
    )
  }

  private def resolveUserReference(
    reference: UserReference,
    memberUidToMemberMap: Map[MemberUid, Member],
    memberNameToMemberMap: Map[String, Member]
  ): IO[DomainError, Member] = {
    reference match {
      case MemberReference(uid) =>
        ZIO.fromOption(memberUidToMemberMap.get(uid))
          .mapError(_ => DomainError(message = s"Invalid member uid: $uid".some))

      case NameReference(name) =>
        ZIO.fromOption(memberNameToMemberMap.get(name))
          .mapError(_ => DomainError(message = s"Invalid member uid: $name".some))
    }
  }
}
