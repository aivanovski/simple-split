package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.entity.NewUser
import com.github.ai.split.entity.db.{UserEntity, UserUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddUserUseCase(
  userDao: UserEntityDao
) {

  def addUser(user: NewUser): IO[DomainError, UserEntity] = {
    for {
      createdUser <- userDao.add(
        UserEntity(
          uid = UserUid(UUID.randomUUID()),
          name = user.name
        )
      )
    } yield createdUser
  }
}
