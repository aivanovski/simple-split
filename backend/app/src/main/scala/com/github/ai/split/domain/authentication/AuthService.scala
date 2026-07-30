package com.github.ai.split.domain.authentication

import com.auth0.jwt.JWT
import com.auth0.jwt.algorithms.Algorithm
import com.github.ai.split.data.db.dao.UserEntityDao
import com.github.ai.split.data.db.model.{UserEntity, UserUid}
import com.github.ai.split.entity.JwtTokenType.{AUTH_TOKEN, REFRESH_TOKEN}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.entity.*
import com.github.ai.split.utils.*
import zio.*
import zio.direct.*

import java.time.Instant
import java.util.{Date, UUID}

class AuthService(
  private val appConfig: ApplicationEnvironment,
  private val userDao: UserEntityDao
) {

  def getTokenTimeToLive(tokenType: JwtTokenType): Duration =
    tokenType match {
      case AUTH_TOKEN => 2.hours
      case REFRESH_TOKEN => 60.days
    }

  def createTokens(userUid: UserUid): JwtTokens =
    JwtTokens(
      token = AuthToken(generateToken(userUid, AUTH_TOKEN)),
      refreshToken = RefreshToken(generateToken(userUid, REFRESH_TOKEN))
    )

  def validateAuthToken(token: AuthToken): IO[DomainError, UserUid] =
    defer {
      val userUid = validateToken(token.toString, AUTH_TOKEN).run
      userDao.getByUid(userUid).run
      userUid
    }

  def validateRefreshToken(token: RefreshToken): IO[DomainError, UserUid] =
    defer {
      val userUid = validateToken(token.toString, REFRESH_TOKEN).run
      userDao.getByUid(userUid).run
      userUid
    }

  def getUserByAuthToken(token: AuthToken): IO[DomainError, UserEntity] =
    defer {
      val userUid = validateToken(token.toString, AUTH_TOKEN).run
      userDao.getByUid(userUid).run
    }

  def validateAuthHeader(header: String): IO[DomainError, UserEntity] =
    defer {
      val token = extractTokenFromHeader(header).run
      getUserByAuthToken(AuthToken(token)).run
    }

  private def validateToken(
    token: String,
    requestedTokenType: JwtTokenType
  ): IO[DomainError, UserUid] =
    defer {
      val verifier = JWT
        .require(Algorithm.HMAC256(appConfig.jwt.secret))
        .withIssuer(appConfig.jwt.issuer)
        .withAudience(appConfig.jwt.audience)
        .build()

      val decodedToken = ZIO
        .attempt(verifier.verify(token))
        .mapError(error => DomainError(cause = error.some))
        .run

      val tokenTypeClaim = Option(decodedToken.getClaim(Claims.TokenType).asString()).getOrElse("")
      val tokenType = ZIO
        .fromOption(JwtTokenType.fromString(tokenTypeClaim))
        .mapError(_ => DomainError(message = "Invalid token type".some))
        .run

      if (tokenType != requestedTokenType) {
        ZIO
          .fail(DomainError(message = "Invalid token type".some))
          .run
      }

      val userUid = ZIO
        .attempt(UserUid(UUID.fromString(Option(decodedToken.getSubject).getOrElse(""))))
        .mapError(error => DomainError(cause = error.some))
        .run

      userUid
    }

  private def generateToken(
    userUid: UserUid,
    tokenType: JwtTokenType
  ): String = {
    val now = Instant.now()
    val timeToLive = getTokenTimeToLive(tokenType)

    JWT
      .create()
      .withIssuer(appConfig.jwt.issuer)
      .withAudience(appConfig.jwt.audience)
      .withSubject(userUid.value.toString)
      .withClaim(Claims.TokenType, tokenType.toString)
      .withIssuedAt(Date.from(now))
      .withExpiresAt(Date.from(now.plusMillis(timeToLive.toMillis)))
      .sign(Algorithm.HMAC256(appConfig.jwt.secret))
  }

  private def extractTokenFromHeader(header: String): IO[DomainError, String] =
    header.split(" ").toList match {
      case "Bearer" :: token :: Nil => ZIO.succeed(token)
      case _ => ZIO.fail(DomainError(message = "Invalid authorization header".some))
    }

  private object Claims {
    val TokenType = "tokenType"
  }
}
