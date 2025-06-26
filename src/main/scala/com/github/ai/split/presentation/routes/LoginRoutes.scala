package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.LoginController
import zio.http.{Method, Request, Routes, handler}
import zio._

object LoginRoutes {

  def routes() = Routes(
    Method.POST / "login" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[LoginController]
        response <- controller.login(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
