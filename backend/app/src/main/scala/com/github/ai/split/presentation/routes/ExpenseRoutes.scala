package com.github.ai.split.presentation.routes

import com.github.ai.split.data.db.model.UserEntity
import com.github.ai.split.domain.authentication.AuthHandler.authHandler
import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.toErrorMessageDto
import com.github.ai.split.presentation.controllers.ExpenseController
import zio.ZIO
import zio.direct.*
import zio.http.*

object ExpenseRoutes {

  def routes() = Routes(
    ApiEndpoints.postExpense.implement { body =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[ExpenseController].run
        controller.createExpense(user, body).mapError(_.toErrorMessageDto).run
      }
    },
    ApiEndpoints.putExpense.implement { case (expenseId, body) =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[ExpenseController].run
        controller.updateExpense(user, expenseId, body).mapError(_.toErrorMessageDto).run
      }
    },
    ApiEndpoints.deleteExpense.implement { expenseId =>
      defer {
        val user = ZIO.service[UserEntity].run
        val controller = ZIO.service[ExpenseController].run
        controller.removeExpense(user, expenseId).mapError(_.toErrorMessageDto).run
      }
    }
  ).@@[ExpenseController](authHandler)
}
