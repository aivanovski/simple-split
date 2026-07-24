package com.github.ai.split

import com.github.ai.split.data.currency.CurrencyParser
import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.domain.ApplicationEnvironmentLoader
import com.github.ai.split.domain.usecases.{FillTestDataUseCase, StartUpServerUseCase}
import com.github.ai.split.entity.ApplicationEnvironment
import com.github.ai.split.entity.HttpProtocol.{HTTP, HTTPS}
import com.github.ai.split.presentation.routes.{CurrencyRoutes, ExpenseRoutes, ExportRoutes, GroupRoutes, MemberRoutes}
import com.github.ai.split.openapi.ApiEndpoints
import com.github.ai.split.utils.RequestLogger
import zio.*
import zio.http.*
import zio.logging.{LogColor, LogFormat, LoggerNameExtractor}
import zio.logging.backend.SLF4J
import zio.direct.*
import zio.http.endpoint.openapi.SwaggerUI
import zio.http.codec.PathCodec.path

import java.time.format.DateTimeFormatter

object Main extends ZIOAppDefault {

  private val routes =
    (GroupRoutes.routes()
      ++ ExportRoutes.routes()
      ++ MemberRoutes.routes()
      ++ ExpenseRoutes.routes()
      ++ CurrencyRoutes.routes()
      ++ SwaggerUI.routes("docs" / "openapi", ApiEndpoints.openApi))
      @@ RequestLogger.requestLogger

  override val bootstrap: ZLayer[Any, Nothing, Unit] = {
    val logFormat: LogFormat =
      LogFormat
        .timestamp(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssAZ"))
        .highlight(_ => LogColor.BLUE)
        |-| LogFormat.bracketStart + LogFormat.loggerName(
          LoggerNameExtractor.trace
        ) + LogFormat.bracketEnd |-|
        LogFormat.fiberId |-| LogFormat.level.highlight |-| LogFormat.line.highlight

    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(logFormat)
  }

  private def application() = defer {
    val startUpUseCase = ZIO.service[StartUpServerUseCase].run
    startUpUseCase.startUpServer().run

    Server.serve(routes).run

    ()
  }

  private def createServerConfig(
    config: ApplicationEnvironment
  ) = defer {
    config.server.protocol match {
      case HTTP =>
        Server.Config.default
          .port(8080)

      case HTTPS =>
        Server.Config.default
          .port(8443)
          .ssl(SSLConfig.fromFile(config.server.certificatePath, config.server.privateKeyPath))
    }
  }

  override def run: ZIO[ZIOAppArgs, Throwable, Unit] = defer {
    val config = ApplicationEnvironmentLoader().loadConfig().run
    val port = config.server.protocol match {
      case HTTP => 8080
      case HTTPS => 8443
    }

    ZIO.logInfo(s"Starting server on port $port").run
    ZIO.logInfo(s"   database.url=${config.database.url}").run
    ZIO.logInfo(s"   database.maximumPoolSize=${config.database.maximumPoolSize}").run
    ZIO.logInfo(s"   database.minimumIdle=${config.database.minimumIdle}").run
    ZIO.logInfo(s"   populateTestData=${config.populateTestData}").run
    ZIO.logInfo(s"   protocol=${config.server.protocol}").run

    val serverConfig = createServerConfig(config).run

    application()
      .provide(
        // Application config
        ZLayer.succeed(config),

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

        // Database
        Layers.appDatabase,

        // Repositories
        Layers.expenseRepository,
        Layers.groupRepository,
        Layers.currencyRepository,

        // Dao
        Layers.expenseDao,
        Layers.groupDao,
        Layers.groupMembershipDao,
        Layers.memberDao,
        Layers.paidByDao,
        Layers.splitBetweenDao,
        Layers.currencyDao,

        // Others
        Layers.currencyParser,
        Server.live,
        ZLayer.succeed(serverConfig)
      )
      .run
    ()
  }
}
