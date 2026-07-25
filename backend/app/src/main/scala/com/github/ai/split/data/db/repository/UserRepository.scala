package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.data.db.model.{UserEntity, UserUid}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.utils.some
import com.github.ai.split.entity.NewUser
import com.github.ai.split.entity.exception.{DomainError, InvalidCredentialsError}
import zio.{IO, ZIO}
import zio.direct.{defer, run}

import java.util.UUID

class UserRepository(
  private val userDao: UserEntityDao,
  private val passwordService: PasswordService
) {

  def add(user: NewUser) =
    userDao.add(
      user = UserEntity(
        uid = UserUid(UUID.randomUUID()),
        name = user.name,
        email = user.email,
        passwordHash = passwordService.hashPassword(user.password)
      )
    )

  def findByEmail(email: String) =
    userDao.findByEmail(email)

  def authenticate(email: String, password: String): IO[DomainError, UserEntity] = defer {
    val userByEmail = findByEmail(email).run

    val user = if (userByEmail.isDefined) {
      userByEmail.get
    } else {
      ZIO.fail(InvalidCredentialsError()).run
    }

    if (passwordService.isPasswordMatch(password, user.passwordHash)) {
      user
    } else {
      ZIO.fail(InvalidCredentialsError()).run
    }
  }
}
