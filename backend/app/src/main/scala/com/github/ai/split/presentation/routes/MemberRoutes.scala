package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.MemberController
import zio.*
import zio.http.{string, *}

object MemberRoutes {

  def routes() = Routes(
    // TODO: refactor, could be done via PUT /group/uid
    Method.POST / "member" / string("groupId") -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[MemberController]
        response <- controller.postMember(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}