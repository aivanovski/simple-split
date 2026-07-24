package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.MemberEntityDao
import com.github.ai.split.entity.db.{MemberEntity, MemberUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class GetAllUsersUseCase(
  private val userDao: MemberEntityDao
) {

  def getAllUsers(): IO[DomainError, List[MemberEntity]] = userDao.getAll()

  // TODO: refactor
  def getUserUidToUserMap(): IO[DomainError, Map[MemberUid, MemberEntity]] = userDao.getMemberUidToMemberMap()
}
