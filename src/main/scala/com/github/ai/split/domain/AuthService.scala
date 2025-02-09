package com.github.ai.split.domain

import com.github.ai.split.utils.toDomainResponse
import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.ai.split.data.UserRepository
import com.github.ai.split.entity.{AuthenticationContext, JwtData, User}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import zio.*
import zio.http.*

import java.time.Clock
import java.util.concurrent.TimeUnit
import scala.util.{Failure, Success, Try}
import java.util.Date

class AuthService(
  private val userRepository: UserRepository
) {

  implicit val clock: Clock = Clock.systemUTC

  val authenticationContext: HandlerAspect[Any, AuthenticationContext] =
    HandlerAspect.interceptIncomingHandler(Handler.fromFunctionZIO[Request] { request =>
      val result = for
        header <- ZIO.fromOption(request.headers.get("Authorization"))
          .mapError(_ => new DomainError(message = "Failed to get auth header".some))

        token <- extractToken(header)

        email <- validateJwtToken(token)

        user <- userRepository.getByEmail(email)
      yield (request, AuthenticationContext(user))

      result.mapError(_.toDomainResponse)
    })

  def createJwtToken(user: User): String = {
    val jwtData = JwtData.DEFAULT
    val expires = clock.millis() + TimeUnit.DAYS.toMillis(30)

    JWT.create()
      .withAudience(jwtData.audience)
      .withIssuer(jwtData.issuer)
      .withClaim(AuthService.EMAIL, user.email)
      .withExpiresAt(Date(expires))
      .sign(Algorithm.HMAC256(jwtData.secret))
  }

  private def extractToken(header: String): IO[DomainError, String] = {
    val parts = header.split(" ")
    if (parts.length != 2) {
      return ZIO.fail(new DomainError(message = "Invalid token".some))
    }

    if (parts(0) != "Bearer") {
      return ZIO.fail(new DomainError(message = "Invalid token type".some))
    }

    ZIO.succeed(parts(1))
  }

  private def validateJwtToken(token: String): IO[DomainError, String] = {
    val jwtData = JwtData.DEFAULT

    val verifier = JWT.require(Algorithm.HMAC256(jwtData.secret))
      .withAudience(jwtData.audience)
      .withIssuer(jwtData.issuer)
      .build()

    ZIO.fromTry(
        Try {
          verifier.verify(token)
        }
      )
      .map(decodedToken =>
        val email = decodedToken.getClaim(AuthService.EMAIL).asString()
        // TODO: check expiration
        email
      )
      .mapError(error => new DomainError(cause = error.some))
  }
}

object AuthService {
  val SECRET_KEY = "secretKey"
  private val EMAIL = "email"
}