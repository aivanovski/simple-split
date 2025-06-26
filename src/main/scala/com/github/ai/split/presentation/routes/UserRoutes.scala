package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.UserController
import zio.*
import zio.http.*

object UserRoutes {
  def routes() = Routes(
    Method.GET / "user" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[UserController]
        response <- controller.getUsers().mapError(_.toDomainResponse)
      } yield response
    } @@ AuthService.authenticationContext,

    Method.POST / "user" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[UserController]
        response <- controller.postUser(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
