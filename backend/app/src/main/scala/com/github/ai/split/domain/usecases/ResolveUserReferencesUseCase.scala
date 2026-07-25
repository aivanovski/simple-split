package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.model.{GroupUid, MemberUid}
import com.github.ai.split.data.db.repository.GroupRepository
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.{MemberWithUser, UserReference, MemberReference, NameReference}
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

class ResolveUserReferencesUseCase(
  private val groupRepository: GroupRepository
) {

  def validateReferences(
    allMembers: List[MemberWithUser],
    references: List[UserReference]
  ): IO[DomainError, Unit] = {
    for {
      _ <- resolveReferences(allMembers, references)
    } yield ()
  }

  def resolveReferences(
    allMembers: List[MemberWithUser],
    references: List[UserReference]
  ): IO[DomainError, List[MemberWithUser]] = {
    defer {
      val memberUidToMemberMap = allMembers.map(member => member.member.uid -> member).toMap
      val memberNameToMemberMap = allMembers.map(member => member.getName() -> member).toMap

      resolveReferences(
        references = references,
        memberUidToMemberMap = memberUidToMemberMap,
        memberNameToMemberMap = memberNameToMemberMap
      ).run
    }
  }

  private def resolveReferences(
    references: List[UserReference],
    memberUidToMemberMap: Map[MemberUid, MemberWithUser],
    memberNameToMemberMap: Map[String, MemberWithUser]
  ): IO[DomainError, List[MemberWithUser]] = {
    ZIO.collectAll(
      references.map { reference =>
        resolveUserReference(reference, memberUidToMemberMap, memberNameToMemberMap)
      }
    )
  }

  private def resolveUserReference(
    reference: UserReference,
    memberUidToMemberMap: Map[MemberUid, MemberWithUser],
    memberNameToMemberMap: Map[String, MemberWithUser]
  ): IO[DomainError, MemberWithUser] = {
    reference match {
      case MemberReference(uid) =>
        ZIO
          .fromOption(memberUidToMemberMap.get(uid))
          .mapError(_ => DomainError(message = s"Invalid member uid: $uid".some))

      case NameReference(name) =>
        ZIO
          .fromOption(memberNameToMemberMap.get(name))
          .mapError(_ => DomainError(message = s"Invalid member uid: $name".some))
    }
  }
}
