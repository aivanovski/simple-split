package com.github.ai.split.presentation.controllers

import com.github.ai.split.data.UserRepository
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.api.UserDto
import com.github.ai.split.entity.User
import com.github.ai.split.entity.api.request.PostUserRequest
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toUserDto
import com.github.ai.split.utils.some
import zio.{ZIO, ZLayer}
import zio.http.*
import zio.json.*

class UserController(
  private val userRepository: UserRepository,
  private val authService: AuthService
) {

  def getUsers(request: Request): ZIO[Any, DomainError, Response] = {
    userRepository.getUsers()
      .map { users => users.map(user => toUserDto(user)) }
      .map(users => Response.text(users.toJsonPretty + "\n"))
  }

  def postUser(request: Request): ZIO[Any, DomainError, Response] = {
    for {
      body <- request.body.asString
        .mapError { error => DomainError(cause = error.some) }

      userDto <- ZIO.fromEither(
        body.fromJson[PostUserRequest]
          .left.map(error => new DomainError(message = error.some))
      )

      existingUser <- userRepository.getUsers()
        .map { users =>
          users.find { user => user.email.equalsIgnoreCase(userDto.email) }
        }

      user <- if (existingUser.isEmpty)
        userRepository.add(
          User(
            email = userDto.email,
            password = userDto.password
          )
        )
      else
        ZIO.fail(new DomainError(message = Some("User exists")))

    } yield
      Response.text(toUserDto(user).toJsonPretty + "\n")
  }
}
