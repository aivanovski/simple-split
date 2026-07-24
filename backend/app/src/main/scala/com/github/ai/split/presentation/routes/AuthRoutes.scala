package com.github.ai.split.presentation.routes

import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.presentation.controllers.AuthController
import com.github.ai.split.utils.toErrorMessageDto
import zio.ZIO
import zio.http.Routes

object AuthRoutes {

  def routes() = Routes(
    ApiEndpoints.signup.implement { body =>
      ZIO
        .serviceWithZIO[AuthController](_.signup(body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.login.implement { body =>
      ZIO
        .serviceWithZIO[AuthController](_.login(body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.refreshToken.implement { body =>
      ZIO
        .serviceWithZIO[AuthController](_.refreshToken(body))
        .mapError(_.toErrorMessageDto)
    }
  )
}
