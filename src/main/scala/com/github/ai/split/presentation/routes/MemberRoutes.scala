package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.getLastUrlParameter
import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.presentation.controllers.MemberController
import zio.*
import zio.http.Handler.{fromFunctionZIO, param}
import zio.http.{string, *}

class MemberRoutes(
  private val memberController: MemberController,
  private val authService: AuthService
) {

  def routes() = Routes(
    Method.POST / "member" / string("groupId") -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      val response = for {
        groupId <- request.getLastUrlParameter()
        user <- ZIO.serviceWith[AuthenticationContext](auth => auth.user)
        response <- memberController.postMember(user, groupId, request)
      } yield response

      response.mapError(_.toDomainResponse)
    } @@ authService.authenticationContext
  )
}