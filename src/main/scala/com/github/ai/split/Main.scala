package com.github.ai.split

import zio.*
import zio.http.*
import zio.logging.LogFormat
import zio.logging.backend.SLF4J

object MainApp extends ZIOAppDefault {

  private val deps = AppDependencies

  override val bootstrap: ZLayer[Any, Nothing, Unit] =
    Runtime.removeDefaultLoggers >>> SLF4J.slf4j(LogFormat.colored)

  override def run = Server
    .serve(
      deps.userRoutes.routes()
        ++ deps.loginRoutes.routes()
        ++ deps.groupRoutes.routes()
        ++ deps.memberRoutes.routes()
        ++ deps.expenseRoutes.routes()
    )
    .provide(
      Server.defaultWithPort(8080)
    )
}