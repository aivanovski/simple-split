package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.AuthService
import com.github.ai.split.domain.usecases.{AddUserUseCase, GetAllUsersUseCase}
import com.github.ai.split.entity.NewUser
import com.github.ai.split.entity.api.UserDto
import com.github.ai.split.entity.api.request.PostUserRequest
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toUserDto
import com.github.ai.split.utils.some
import zio.{ZIO, ZLayer}
import zio.http.*
import zio.json.*

class UserController(
  private val authService: AuthService,
  private val addUserUseCase: AddUserUseCase,
  private val getAllUsersUseCase: GetAllUsersUseCase
) {

  def getUsers(): ZIO[Any, DomainError, Response] = {
    // TODO: should return users visible to current session
    for {
      users <- getAllUsersUseCase.getAllUsers()
        .map(users => users.map(user => toUserDto(user)))
    } yield Response.text(users.toJsonPretty + "\n")
  }

  def postUser(request: Request): ZIO[Any, DomainError, Response] = {
    for {
      body <- request.body.asString
        .mapError { error => DomainError(cause = error.some) }

      userDto <- ZIO.fromEither(
        body.fromJson[PostUserRequest]
          .left.map(error => new DomainError(message = error.some))
      )

      user <- addUserUseCase.addUser(
        NewUser(
          email = userDto.email,
          password = userDto.password
        )
      )

    } yield Response.text(toUserDto(user).toJsonPretty + "\n")
  }
}
