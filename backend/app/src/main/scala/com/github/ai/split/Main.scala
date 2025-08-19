package com.github.ai.split

import com.github.ai.split.domain.CliArgumentParser
import com.github.ai.split.domain.usecases.FillTestDataUseCase
import com.github.ai.split.entity.CliArguments
import com.github.ai.split.presentation.routes.{ExpenseRoutes, ExportRoutes, GroupRoutes, MemberRoutes}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*
import zio.http.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object Main extends ZIOAppDefault {

  private val routes = GroupRoutes.routes()
    ++ ExportRoutes.routes()
    ++ MemberRoutes.routes()
    ++ ExpenseRoutes.routes()

  override val bootstrap: ZLayer[Any, Nothing, Unit] = {
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)
  }

  private def application() = {
    for {
      fillTestDataUseCase <- ZIO.service[FillTestDataUseCase]
      arguments <- ZIO.service[CliArguments]

      _ <-
        if (arguments.isPopulateTestData) {
          fillTestDataUseCase.createTestData()
        } else {
          ZIO.succeed(())
        }

      _ <- Server.serve(routes)
    } yield ()
  }

  override def run: ZIO[ZIOAppArgs, Throwable, Unit] = {
    for {
      arguments <- CliArgumentParser().parse()
      _ <- ZIO.logInfo(s"Starting application with arguments:")
      _ <- ZIO.logInfo(arguments.toReadableString())

      _ <- application().provide(
        // Application arguments
        ZLayer.succeed(arguments),

        // Use-Cases
        Layers.addUserUseCase,
        Layers.getAllUsersUseCase,
        Layers.addGroupUseCase,
        Layers.getGroupByUidUseCase,
        Layers.addMemberUseCase,
        Layers.addExpenseUseCase,
        Layers.convertToTransactionsUseCase,
        Layers.calculateSettlementUseCase,
        Layers.fillTestDataUseCase,
        Layers.updateGroupUseCase,
        Layers.updateExpenseUseCase,
        Layers.removeMembersUseCase,
        Layers.resolveUserReferencesUseCase,
        Layers.validateMemberNameUseCase,
        Layers.validateExpenseUseCase,
        Layers.removeExpenseUseCase,
        Layers.exportGroupDataUseCase,
        Layers.updateMemberUseCase,

        // Response assemblers use cases
        Layers.assembleGroupResponseUseCase,
        Layers.assembleGroupsResponseUseCase,
        Layers.assembleExpenseUseCase,

        // Controllers
        Layers.memberController,
        Layers.groupController,
        Layers.expenseController,

        // Services
        Layers.passwordService,
        Layers.accessResolverService,

        // Repositories
        Layers.expenseRepository,
        Layers.groupRepository,

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
        if (arguments.isUseInMemoryDatabase) {
          Quill.DataSource.fromPrefix("test-h2db")
        } else {
          Quill.DataSource.fromPrefix("h2db")
        }
      )
    } yield ()
  }
}
