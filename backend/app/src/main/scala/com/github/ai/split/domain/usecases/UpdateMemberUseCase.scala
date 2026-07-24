package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.GroupMembershipEntityDao
import com.github.ai.split.entity.db.MembershipUid
import zio.*
import zio.direct.*
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.db.GroupMembershipEntity
import com.github.ai.split.data.db.dao.MemberEntityDao
import com.github.ai.split.data.db.repository.GroupRepository

class UpdateMemberUseCase(
  private val groupRepository: GroupRepository,
  private val memberDao: GroupMembershipEntityDao,
  private val userDao: MemberEntityDao,
  private val validateMemberUseCase: ValidateMemberNameUseCase
) {

  def updateMember(
    memberUid: MembershipUid,
    newName: String
  ): IO[DomainError, GroupMembershipEntity] = {
    defer {
      val member = memberDao.getByUid(memberUid).run
      val members = groupRepository.getMembers(member.groupUid).run
      val user = userDao.getByUid(member.memberUid).run

      val currentNames = members
        .filter(member => member.entity.uid != memberUid)
        .map(member => member.user.name)

      validateMemberUseCase
        .validateNewMembers(
          currentMemberNames = currentNames,
          newMemberNames = List(newName)
        )
        .run

      userDao
        .update(
          user.copy(name = newName)
        )
        .run

      member
    }
  }
}
