package com.github.ai.split

import com.github.ai.split.data.currency.CurrencyParser
import com.github.ai.split.domain.CliArgumentParser
import com.github.ai.split.domain.usecases.{FillTestDataUseCase, StartUpServerUseCase}
import com.github.ai.split.entity.CliArguments
import com.github.ai.split.entity.HttpProtocol.{HTTP, HTTPS}
import com.github.ai.split.presentation.routes.{CurrencyRoutes, ExpenseRoutes, ExportRoutes, GroupRoutes, MemberRoutes}
import io.getquill.SnakeCase
import io.getquill.jdbczio.Quill
import zio.*
import zio.http.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J
import zio.direct.*

object Main extends ZIOAppDefault {

  private val routes = GroupRoutes.routes()
    ++ ExportRoutes.routes()
    ++ MemberRoutes.routes()
    ++ ExpenseRoutes.routes()
    ++ CurrencyRoutes.routes()

  override val bootstrap: ZLayer[Any, Nothing, Unit] = {
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)
  }

  private def application() = defer {
    val startUpUseCase = ZIO.service[StartUpServerUseCase].run
    startUpUseCase.startUpServer().run

    Server.serve(routes).run

    ()
  }

  private def createServerConfig(
    arguments: CliArguments
  ) = defer {
    arguments.protocol match {
      case HTTP =>
        Server.Config.default
          .port(arguments.getPort())

      case HTTPS =>
        Server.Config.default
          .port(arguments.getPort())
          .ssl(SSLConfig.fromFile("dev-data/server.crt", "dev-data/server.key"))
    }
  }

  override def run: ZIO[ZIOAppArgs, Throwable, Unit] = {
    for {
      arguments <- CliArgumentParser().parse()
      _ <- ZIO.logInfo(s"Starting server on port ${arguments.getPort()}")
      _ <- ZIO.logInfo(s"   isUseInMemoryDatabase=${arguments.isUseInMemoryDatabase}")
      _ <- ZIO.logInfo(s"   isPopulateTestData=${arguments.isPopulateTestData}")
      _ <- ZIO.logInfo(s"   protocol=${arguments.protocol}")

      serverConfig <- createServerConfig(arguments)

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
        Layers.startUpServerUseCase,
        Layers.fillCurrencyDataUseCase,
        Layers.validateCurrencyUseCase,

        // Response assemblers use cases
        Layers.assembleGroupResponseUseCase,
        Layers.assembleGroupsResponseUseCase,
        Layers.assembleExpenseUseCase,

        // Controllers
        Layers.memberController,
        Layers.groupController,
        Layers.expenseController,
        Layers.currencyController,

        // Services
        Layers.passwordService,
        Layers.accessResolverService,

        // Repositories
        Layers.expenseRepository,
        Layers.groupRepository,
        Layers.currencyRepository,

        // Dao
        Layers.expenseDao,
        Layers.groupDao,
        Layers.groupMemberDao,
        Layers.userDao,
        Layers.paidByDao,
        Layers.splitBetweenDao,
        Layers.currencyDao,

        // Others
        Layers.currencyParser,
        Server.live,
        ZLayer.succeed(serverConfig),
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
