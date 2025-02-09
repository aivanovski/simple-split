package com.github.ai.split.presentation.routes

import com.github.ai.split.domain.AuthService
import com.github.ai.split.entity.AuthenticationContext
import com.github.ai.split.utils.getLastUrlParameter
import com.github.ai.split.utils.toDomainResponse
import com.github.ai.split.presentation.controllers.ExpenseController
import zio.ZIO
import zio.http.*

class ExpenseRoutes(
  private val expenseController: ExpenseController,
  private val authService: AuthService
) {

  def routes() = Routes(
    Method.POST / "expense" / string("groupId") -> Handler.fromFunctionZIO[Request] { (request: Request) =>
      val result = for {
        groupId <- request.getLastUrlParameter()
        user <- ZIO.serviceWith[AuthenticationContext](auth => auth.user)
        response <- expenseController.postExpense(groupId, user, request)
      } yield response

      result.mapError(_.toDomainResponse)
    } @@ authService.authenticationContext
  )
}
