package com.github.ai.split.utils

import zio.{Clock, ZIO}
import zio.direct.*
import zio.http.{Middleware, Request, Routes, handler}

object RequestLogger {

  val requestLogger: Middleware[Any] = new Middleware[Any] {
    def apply[Env1 <: Any, Err](routes: Routes[Env1, Err]): Routes[Env1, Err] =
      routes.transform[Env1] { requestHandler =>
        handler { (request: Request) =>
          defer {
            val startTime = Clock.nanoTime.run
            ZIO.logInfo(s"-> ${request.method} ${request.url.encode}").run

            val response = requestHandler
              .apply(request)
              .tapError(err => ZIO.logError(s"<- ${request.method} ${request.url.encode} error=$err"))
              .run

            val endTime = Clock.nanoTime.run
            val elapsedTime = (endTime - startTime) / 1000000L

            ZIO
              .logInfo(
                s"<- %s %s %s %sms"
                  .format(
                    request.method,
                    request.url.encode,
                    response.status.code,
                    elapsedTime
                  )
              )
              .run

            response
          }
        }
      }
  }
}
