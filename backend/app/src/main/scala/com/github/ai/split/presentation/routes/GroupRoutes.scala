package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.GroupController
import zio.ZIO
import zio.http.{Handler, Method, Request, Response, Routes, handler, string}

object GroupRoutes {

  def routes() = Routes(
    Method.GET / "group" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[GroupController]
        response <- controller.getGroups(request).mapError(_.toDomainResponse)
      } yield response
    },

    Method.POST / "group" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[GroupController]
        response <- controller.createGroup(request).mapError(_.toDomainResponse)
      } yield response
    },

    Method.PUT / "group" / string("groupId") ->  handler { (request: Request) =>
      for {
        controller <- ZIO.service[GroupController]
        response <- controller.updateGroup(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
