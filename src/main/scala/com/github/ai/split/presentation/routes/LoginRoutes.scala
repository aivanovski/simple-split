package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.LoginController
import zio.http.{Method, Request, Routes, handler}

class LoginRoutes(
  private val loginController: LoginController
) {

  def routes() = Routes(
    Method.POST / "login" -> handler { (request: Request) =>
      loginController.login(request)
        .mapError(_.toDomainResponse)
    }
  )
}
