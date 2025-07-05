package com.github.ai.split

import com.github.ai.split.presentation.routes.{ExpenseRoutes, GroupRoutes, UserRoutes, MemberRoutes}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*
import zio.http.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val deps = Layers

  private val routes = UserRoutes.routes()
    ++ GroupRoutes.routes()
    ++ MemberRoutes.routes()
    ++ ExpenseRoutes.routes()

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)

  override def run = Server
    .serve(routes)
    .provide(
      // Use-Cases
      Layers.addUserUseCase,
      Layers.getAllUsersUseCase,
      Layers.addGroupUseCase,
      Layers.getGroupByUidUseCase,
      Layers.addMemberUseCase,
      Layers.addExpenseUseCase,
      Layers.convertToTransactionsUseCase,
      Layers.calculateSettlementUseCase,

      // Response assemblers use cases
      Layers.assembleGroupResponseUseCase,
      Layers.assembleGroupsResponseUseCase,
      Layers.assembleExpenseUseCase,

      // Controllers
      Layers.memberController,
      Layers.groupController,
      Layers.userController,
      Layers.expenseController,

      // Services
      Layers.authService,
      Layers.passwordService,
      Layers.accessResolverService,

      // Dao
      Layers.expenseDao,
      Layers.groupDao,
      Layers.groupMemberDao,
      Layers.userDao,
      Layers.paidByDao,
      Layers.splitBetweenDao,

      // Others
      Server.defaultWithPort(8080),
      Quill.H2.fromNamingStrategy(SnakeCase),
      Quill.DataSource.fromPrefix("h2db")
    )
}