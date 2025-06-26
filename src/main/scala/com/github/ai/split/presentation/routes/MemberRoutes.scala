package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.MemberController
import zio.*
import zio.http.{string, *}

object MemberRoutes {

  def routes() = Routes(
    Method.POST / "member" / string("groupId") -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[MemberController]
        auth <- ZIO.service[AuthenticationContext]
        response <- controller.postMember(auth.user, request).mapError(_.toDomainResponse)
      } yield response
    } @@ AuthService.authenticationContext
  )
}