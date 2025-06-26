package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.utils.some
import com.github.ai.split.entity.NewUser
import com.github.ai.split.entity.db.UserEntity
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddUserUseCase(
  userDao: UserEntityDao
) {

  def addUser(user: NewUser): IO[DomainError, UserEntity] = {
    for {
      isExists <- isUserExists(user.email)
      createdUser <- if (!isExists) {
        userDao.add(
          UserEntity(
            uid = UUID.randomUUID(),
            email = user.email,
            password = user.password
          )
        )
      } else {
        ZIO.fail(DomainError(message = s"User already exists".some))
      }
    } yield createdUser
  }

  private def isUserExists(email: String): IO[DomainError, Boolean] = {
    for {
      users <- userDao.getAll()
    } yield {
      users.find(user => user.email == email) match
        case Some(_) => true
        case None => false
    }
  }
}
