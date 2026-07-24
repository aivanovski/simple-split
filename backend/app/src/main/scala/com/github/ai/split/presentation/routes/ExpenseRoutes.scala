package com.github.ai.split.presentation.routes

import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.toErrorMessageDto
import com.github.ai.split.presentation.controllers.ExpenseController
import zio.ZIO
import zio.http.*

object ExpenseRoutes {

  def routes() = Routes(
    ApiEndpoints.postExpense.implement { case (password, body) =>
      ZIO
        .serviceWithZIO[ExpenseController](_.createExpense(password.getOrElse(""), body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.putExpense.implement { case (expenseId, password, body) =>
      ZIO
        .serviceWithZIO[ExpenseController](_.updateExpense(expenseId, password.getOrElse(""), body))
        .mapError(_.toErrorMessageDto)
    },
    ApiEndpoints.deleteExpense.implement { case (expenseId, password) =>
      ZIO
        .serviceWithZIO[ExpenseController](_.removeExpense(expenseId, password.getOrElse("")))
        .mapError(_.toErrorMessageDto)
    }
  )
}
