package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.entity.db.{UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class GetAllUsersUseCase(
  private val userDao: UserEntityDao
) {

  def getAllUsers(): IO[DomainError, List[UserEntity]] = userDao.getAll()

  // TODO: refactor
  def getUserUidToUserMap(): IO[DomainError, Map[UserUid, UserEntity]] = userDao.getUserUidToUserMap()
}
