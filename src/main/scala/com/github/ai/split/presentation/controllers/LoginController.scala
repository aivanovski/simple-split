package com.github.ai.split.presentation.controllers

import com.github.ai.split.data.UserRepository
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.User
import com.github.ai.split.entity.api.request.{LoginRequest, PostUserRequest}
import com.github.ai.split.entity.api.response.LoginResponse
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import com.github.ai.split.utils.parse
import zio.*
import zio.http.{Request, Response}
import zio.json.*

class LoginController(
  private val userRepository: UserRepository,
  private val authService: AuthService
) {

  def login(request: Request): ZIO[Any, DomainError, Response] = {
    for
      data <- request.body.parse[LoginRequest]
      user <- areCredentialsValid(data.email, data.password)
      response <- createResponse(user)
    yield
      Response.text(response.toJsonPretty + "\n")
  }

  private def areCredentialsValid(
    email: String,
    password: String
  ): IO[DomainError, User] = {
    userRepository.getByEmail(email)
      .flatMap(user =>
        if (user.password == password) {
          ZIO.succeed(user)
        } else {
          ZIO.fail(DomainError(message = "Failed to authenticate".some))
        }
      )
  }

  private def createResponse(user: User): IO[DomainError, LoginResponse] =
    ZIO.succeed(
      LoginResponse(
        token = authService.createJwtToken(user),
        userUid = user.uid.toString
      )
    )
}