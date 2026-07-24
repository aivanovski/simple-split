package com.github.ai.split.presentation.routes

import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.toErrorMessageDto
import com.github.ai.split.presentation.controllers.{CurrencyController}
import zio.ZIO
import zio.http.Routes

object CurrencyRoutes {

  def routes() = Routes(
    ApiEndpoints.getCurrencies.implement { _ =>
      ZIO.serviceWithZIO[CurrencyController](_.getCurrencies()).mapError(_.toErrorMessageDto)
    }
  )
}
