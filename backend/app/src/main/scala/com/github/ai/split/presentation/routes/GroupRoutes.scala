package com.github.ai.split.presentation.routes

import com.github.ai.split.data.db.model.UserEntity
import com.github.ai.split.domain.authentication.AuthHandler.authHandler
import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.presentation.controllers.GroupController
import com.github.ai.split.utils.toErrorMessageDto
import zio.ZIO
import zio.direct.*
import zio.http.Routes

object GroupRoutes {

  def routes() = Routes(
    ApiEndpoints.getGroups.implement { case (ids, _) =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[GroupController].run
        controller.getGroups(user, ids).mapError(_.toErrorMessageDto).run
      }
    },
    ApiEndpoints.postGroup.implement { body =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[GroupController].run
        controller.createGroup(user, body).mapError(_.toErrorMessageDto).run
      }
    },
    ApiEndpoints.putGroup.implement { case (groupId, _, body) =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[GroupController].run
        controller
          .updateGroup(user, groupId, body)
          .mapError(_.toErrorMessageDto)
          .run
      }
    }
  ).@@[GroupController](authHandler)
}
