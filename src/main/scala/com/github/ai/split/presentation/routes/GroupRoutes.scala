package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.GroupController
import zio.ZIO
import zio.http.{Handler, Method, Request, Routes, handler}

class GroupRoutes(
  private val groupController: GroupController,
  private val authService: AuthService
) {

  def routes() = Routes(
    Method.GET / "group" -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      ZIO.serviceWith[AuthenticationContext](auth => auth.user)
        .flatMap(user => groupController.getGroups(request))
        .mapError(_.toDomainResponse)
    } @@ authService.authenticationContext,

    Method.POST / "group" -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      ZIO.serviceWith[AuthenticationContext](auth => auth.user)
        .flatMap(user => groupController.postGroup(user, request))
        .mapError(_.toDomainResponse)
    } @@ authService.authenticationContext
  )
}
