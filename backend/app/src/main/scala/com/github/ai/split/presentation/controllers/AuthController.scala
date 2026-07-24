package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.UserDto
import com.github.ai.split.api.request.{LoginRequest, RefreshTokenRequest, SignupRequest}
import com.github.ai.split.api.response.{LoginResponse, RefreshTokenResponse, SignupResponse}
import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.data.db.model.{UserEntity, UserUid}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.domain.authentication.AuthService
import com.github.ai.split.entity.RefreshToken
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

import java.util.UUID

class AuthController(
  private val userDao: UserEntityDao,
  private val passwordService: PasswordService,
  private val authService: AuthService
) {

  def signup(body: SignupRequest): IO[DomainError, SignupResponse] =
    defer {
      val existingUser = userDao.findByEmail(body.email).run
      if (existingUser.isDefined) {
        ZIO.fail(DomainError(message = "User already exists".some)).run
      }

      val user = UserEntity(
        uid = UserUid(UUID.randomUUID()),
        name = body.name,
        email = body.email,
        passwordHash = passwordService.hashPassword(body.password)
      )

      userDao.add(user).run

      val tokens = authService.createTokens(user.uid)
      SignupResponse(
        token = tokens.token.toString,
        refreshToken = tokens.refreshToken.toString,
        user = toUserDto(user)
      )
    }

  def login(body: LoginRequest): IO[DomainError, LoginResponse] =
    defer {
      val user = userDao
        .findByEmail(body.email)
        .flatMap {
          case Some(user) => ZIO.succeed(user)
          case None => invalidCredentials
        }
        .run

      if (!passwordService.isPasswordMatch(body.password, user.passwordHash)) {
        invalidCredentials.run
      }

      val tokens = authService.createTokens(user.uid)
      LoginResponse(
        token = tokens.token.toString,
        refreshToken = tokens.refreshToken.toString,
        user = toUserDto(user)
      )
    }

  def refreshToken(body: RefreshTokenRequest): IO[DomainError, RefreshTokenResponse] =
    defer {
      val userUid = authService
        .validateRefreshToken(RefreshToken(body.refreshToken))
        .run

      val tokens = authService.createTokens(userUid)
      RefreshTokenResponse(
        token = tokens.token.toString,
        refreshToken = tokens.refreshToken.toString
      )
    }

  private def invalidCredentials: IO[DomainError, UserEntity] =
    ZIO.fail(DomainError(message = "Invalid email or password".some))

  private def toUserDto(user: UserEntity): UserDto =
    UserDto(
      name = user.name,
      email = user.email
    )
}
