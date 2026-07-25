package com.github.ai.split.domain.usecases

import zio.*
import zio.direct.*
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import com.github.ai.split.data.db.dao.MemberEntityDao
import com.github.ai.split.data.db.model.{MemberEntity, MemberUid}
import com.github.ai.split.data.db.repository.GroupRepository

class UpdateMemberUseCase(
  private val groupRepository: GroupRepository,
  private val memberDao: MemberEntityDao,
  private val validateMemberUseCase: ValidateMemberNameUseCase
) {

  def updateMember(
    memberUid: MemberUid,
    newName: String
  ): IO[DomainError, MemberEntity] = {
    defer {
      val member = memberDao.getByUid(memberUid).run
      if (member.userUid.nonEmpty) {
        ZIO
          .fail(DomainError(message = "A member linked to a user cannot have a separate name".some))
          .run
      }
      val members = groupRepository.getMembers(member.groupUid).run

      val currentNames = members
        .filter(member => member.member.uid != memberUid)
        .map(_.getName())

      validateMemberUseCase
        .validateNewMembers(
          currentMemberNames = currentNames,
          newMemberNames = List(newName)
        )
        .run

      memberDao
        .update(
          member.copy(name = Some(newName))
        )
        .run

      member
    }
  }
}
