package com.github.ai.split.presentation.routes

import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.ExpenseController
import zio.ZIO
import zio.http.*

object ExpenseRoutes {

  def routes() = Routes(
    Method.POST / "expense" / string("groupId") -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      for {
        controller <- ZIO.service[ExpenseController]
        response <- controller.postExpense(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
