package com.github.ai.split.presentation.controllers

import com.github.ai.split.api.UserDto
import com.github.ai.split.api.request.{LoginRequest, RefreshTokenRequest, SignupRequest}
import com.github.ai.split.api.response.{LoginResponse, RefreshTokenResponse, SignupResponse}
import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.data.db.model.{UserEntity, UserUid}
import com.github.ai.split.data.db.repository.UserRepository
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.domain.authentication.AuthService
import com.github.ai.split.entity.{NewUser, RefreshToken}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

import java.util.UUID

class AuthController(
  private val userRepository: UserRepository,
  private val authService: AuthService
) {

  def signup(body: SignupRequest): IO[DomainError, SignupResponse] =
    defer {
      val existingUser = userRepository.findByEmail(body.email).run
      if (existingUser.isDefined) {
        ZIO.fail(DomainError(message = "User already exists".some)).run
      }

      // TODO: add email validation

      val user = userRepository
        .add(
          NewUser(
            name = body.name,
            email = body.email,
            password = body.password
          )
        )
        .run

      val tokens = authService.createTokens(user.uid)
      SignupResponse(
        token = tokens.token.toString,
        refreshToken = tokens.refreshToken.toString,
        user = toUserDto(user)
      )
    }

  def login(body: LoginRequest): IO[DomainError, LoginResponse] =
    defer {
      val user = userRepository
        .authenticate(
          email = body.email,
          password = body.password
        )
        .run

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

  private def toUserDto(user: UserEntity): UserDto =
    UserDto(
      name = user.name,
      email = user.email
    )
}
