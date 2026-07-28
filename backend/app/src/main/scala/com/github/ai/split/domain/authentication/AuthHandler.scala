package com.github.ai.split.domain.authentication

import com.github.ai.split.data.db.model.UserEntity
import com.github.ai.split.entity.AuthToken
import com.github.ai.split.entity.exception.MissingAuthTokenError
import com.github.ai.split.utils.toDomainResponse
import zio.ZIO
import zio.direct.*
import zio.http.{Handler, HandlerAspect, Header, Request}

object AuthHandler {

  val AuthTokenCookieName = "authToken"
  val RefreshTokenCookieName = "refreshToken"

  val authHandler: HandlerAspect[AuthService, UserEntity] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      handleAuth(request)
        .map(user => (request, user))
        .mapError(_.toDomainResponse)
    })

  private def handleAuth(request: Request) = defer {
    val cookieToken = request
      .cookie(AuthTokenCookieName)
      .map(cookie => AuthToken(cookie.content))

    val headerToken = request
      .header(Header.Authorization)
      .flatMap {
        case Header.Authorization.Bearer(token) => Some(AuthToken(token.value.asString))
        case _ => None
      }

    if (cookieToken.isEmpty && headerToken.isEmpty) {
      ZIO.fail(MissingAuthTokenError()).run
    }

    val token = headerToken
      .orElse(cookieToken)
      .getOrElse(AuthToken(""))

    val authService = ZIO.service[AuthService].run
    authService.getUserByAuthToken(token).run
  }
}
