package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.{CurrencyController}
import zio.ZIO
import zio.http.{Method, Request, Routes, handler}

object CurrencyRoutes {

  def routes() = Routes(
    Method.GET / "currency" -> handler { (request: Request) =>
      for {
        controller <- ZIO.service[CurrencyController]
        response <- controller.getCurrencies().mapError(_.toDomainResponse)
      } yield response
    },
  )
}
