package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.entity.db.UserEntity
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class GetAllUsersUseCase(
  private val userDao: UserEntityDao
) {

  def getAllUsers(): IO[DomainError, List[UserEntity]] = userDao.getAll()

  def getUserUidToUserMap(): IO[DomainError, Map[UUID, UserEntity]] = userDao.getUserUidToUserMap()
}
