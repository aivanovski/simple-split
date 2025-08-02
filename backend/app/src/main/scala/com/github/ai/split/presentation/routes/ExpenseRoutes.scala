package com.github.ai.split.presentation.routes

import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.ExpenseController
import zio.ZIO
import zio.http.*

object ExpenseRoutes {

  def routes() = Routes(
    Method.POST / "expense" -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      for {
        controller <- ZIO.service[ExpenseController]
        response <- controller.createExpense(request).mapError(_.toDomainResponse)
      } yield response
    },
    Method.PUT / "expense" / string("expenseId") -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      for {
        controller <- ZIO.service[ExpenseController]
        response <- controller.updateExpense(request).mapError(_.toDomainResponse)
      } yield response
    },
    Method.DELETE / "expense" / string("expenseId") -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      for {
        controller <- ZIO.service[ExpenseController]
        response <- controller.removeExpense(request).mapError(_.toDomainResponse)
      } yield response
    }
  )
}
