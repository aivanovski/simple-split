package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.GroupMemberEntityDao
import com.github.ai.split.entity.db.MemberUid
import zio.*
import zio.direct.*
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.db.GroupMemberEntity
import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.data.db.repository.GroupRepository

class UpdateMemberUseCase(
  private val groupRepository: GroupRepository,
  private val memberDao: GroupMemberEntityDao,
  private val userDao: UserEntityDao,
  private val validateMemberUseCase: ValidateMemberNameUseCase
) {

  def updateMember(
    memberUid: MemberUid,
    newName: String
  ): IO[DomainError, GroupMemberEntity] = {
    defer {
      val member = memberDao.getByUid(memberUid).run
      val members = groupRepository.getMembers(member.groupUid).run
      val user = userDao.getByUid(member.userUid).run

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
