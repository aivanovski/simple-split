package com.github.ai.split.presentation.routes

import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.toErrorMessageDto
import com.github.ai.split.presentation.controllers.GroupController
import zio.ZIO
import zio.http.Routes

object GroupRoutes {

  def routes() = Routes(
    ApiEndpoints.getGroups.implement { case (ids, passwords) =>
      ZIO
        .serviceWithZIO[GroupController](_.getGroups(ids, passwords))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.postGroup.implement { body =>
      ZIO
        .serviceWithZIO[GroupController](_.createGroup(body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.putGroup.implement { case (groupId, password, body) =>
      ZIO
        .serviceWithZIO[GroupController](_.updateGroup(groupId, password.getOrElse(""), body))
        .mapError(_.toErrorMessageDto)
    }
  )
}
