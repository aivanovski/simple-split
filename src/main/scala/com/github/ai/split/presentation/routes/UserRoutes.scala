package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.UserController
import zio.*
import zio.http.*

class UserRoutes(
  private val userController: UserController,
  private val authService: AuthService
) {

  def routes() = Routes(
    Method.GET / "user" -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      ZIO.serviceWith[AuthenticationContext](auth => auth.user)
        .flatMap(user => userController.getUsers(request))
        .mapError(_.toDomainResponse)
    } @@ authService.authenticationContext,

    Method.POST / "user" -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      userController.postUser(request)
        .mapError(_.toDomainResponse)
    },
  )
}
