package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.AuthService
import com.github.ai.split.domain.usecases.GetUserByEmailUseCase
import com.github.ai.split.entity.api.request.{LoginRequest, PostUserRequest}
import com.github.ai.split.entity.api.response.LoginResponse
import com.github.ai.split.entity.db.UserEntity
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import com.github.ai.split.utils.parse
import zio.*
import zio.http.{Request, Response}
import zio.json.*

class LoginController(
  private val getUserByEmailUseCase: GetUserByEmailUseCase,
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
  ): IO[DomainError, UserEntity] = {
    for {
      user <- getUserByEmailUseCase.getUserByEmail(email)
      result <- if (user.password == password) {
        ZIO.succeed(user)
      } else {
        ZIO.fail(DomainError(message = "Unable to authenticate".some))
      }
    } yield result
  }

  private def createResponse(user: UserEntity): IO[DomainError, LoginResponse] =
    ZIO.succeed(
      LoginResponse(
        token = authService.createJwtToken(user),
        userUid = user.uid.toString
      )
    )
}