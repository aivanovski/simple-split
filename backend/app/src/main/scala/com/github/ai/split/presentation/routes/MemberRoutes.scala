package com.github.ai.split.presentation.routes

import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.toErrorMessageDto
import com.github.ai.split.presentation.controllers.MemberController
import zio.*
import zio.http.Routes

object MemberRoutes {

  def routes() = Routes(
    ApiEndpoints.postMember.implement { case (password, body) =>
      ZIO
        .serviceWithZIO[MemberController](_.createMember(password.getOrElse(""), body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.putMember.implement { case (memberId, password, body) =>
      ZIO
        .serviceWithZIO[MemberController](_.updateMember(memberId, password.getOrElse(""), body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.deleteMember.implement { case (memberId, password) =>
      ZIO
        .serviceWithZIO[MemberController](_.removeMember(memberId, password.getOrElse("")))
        .mapError(_.toErrorMessageDto)
    }
  )
}
