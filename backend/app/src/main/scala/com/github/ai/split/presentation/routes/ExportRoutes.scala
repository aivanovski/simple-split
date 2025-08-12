package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.GroupController
import zio.*
import zio.http.*

object ExportRoutes {

  def routes() = Routes(
    Method.GET / "export" / string("groupIdAndExtension") -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[GroupController]
        response <- controller.exportGroup(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
