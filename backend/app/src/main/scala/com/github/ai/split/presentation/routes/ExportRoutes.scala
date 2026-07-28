package com.github.ai.split.presentation.routes

import com.github.ai.split.data.db.model.UserEntity
import com.github.ai.split.domain.authentication.AuthHandler.authHandler
import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.GroupController
import zio.*
import zio.direct.*
import zio.http.*

object ExportRoutes {

  def routes() = Routes(
    Method.GET / "export" / string("groupIdAndExtension") -> handler { (request: Request) =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[GroupController].run

        controller.exportGroup(user, request).mapError(_.toDomainResponse).run
      }
    }
  ) @@ authHandler
}
