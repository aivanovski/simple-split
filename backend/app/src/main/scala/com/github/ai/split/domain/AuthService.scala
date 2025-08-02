package com.github.ai.split.domain

import com.github.ai.split.utils.toDomainResponse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.auth0.jwt.interfaces.DecodedJWT
import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.entity.db.{UserEntity, UserUid}
import com.github.ai.split.entity.{AuthenticationContext, JwtData}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import zio.*
import zio.http.*

import java.time.Clock
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}
import java.util.Date

class AuthService(
  private val userDao: UserEntityDao
) {

  implicit val clock: Clock = Clock.systemUTC

  def createJwtToken(user: UserEntity): String = {
    val jwtData = JwtData.DEFAULT
    val expires = clock.millis() + TimeUnit.DAYS.toMillis(30)

    JWT
      .create()
      .withAudience(jwtData.audience)
      .withIssuer(jwtData.issuer)
      .withClaim(AuthService.USER_UID, user.uid.toString)
      .withExpiresAt(Date(expires))
      .sign(Algorithm.HMAC256(jwtData.secret))
  }

  def validateAuthHeader(header: String): IO[DomainError, UserEntity] = {
    for
      token <- extractTokenFromHeader(header)
      decodedToken <- decodeJwtToken(token)
      user <- getUserByToken(decodedToken)
    yield user
  }

  private def extractTokenFromHeader(header: String): IO[DomainError, String] = {
    val parts = header.split(" ")
    if (parts.length != 2) {
      return ZIO.fail(new DomainError(message = "Invalid token".some))
    }

    if (parts(0) != "Bearer") {
      return ZIO.fail(new DomainError(message = "Invalid token type".some))
    }

    ZIO.succeed(parts(1))
  }

  private def decodeJwtToken(token: String): IO[DomainError, DecodedJWT] = {
    val jwtData = JwtData.DEFAULT

    val verifier = JWT
      .require(Algorithm.HMAC256(jwtData.secret))
      .withAudience(jwtData.audience)
      .withIssuer(jwtData.issuer)
      .build()

    // TODO: check expiration

    ZIO
      .fromTry(
        Try {
          verifier.verify(token)
        }
      )
      .mapError(error => new DomainError(cause = error.some))
  }

  private def getUserByToken(token: DecodedJWT): IO[DomainError, UserEntity] = {
    for {
      userUid <- token.getClaim(AuthService.USER_UID).asString().parseUid()
      user <- userDao.getByUid(UserUid(userUid))
    } yield user
  }
}

object AuthService {
  private val USER_UID = "user_uid"

  val authenticationContext: HandlerAspect[AuthService, AuthenticationContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      ZIO
        .serviceWithZIO[AuthService] { authService =>
          for
            header <- ZIO
              .fromOption(request.headers.get("Authorization"))
              .mapError(_ => DomainError(message = "Failed to get auth header".some))

            user <- authService.validateAuthHeader(header)
          yield (request, AuthenticationContext(user))
        }
        .mapError(error => error.toDomainResponse)
    })
}
