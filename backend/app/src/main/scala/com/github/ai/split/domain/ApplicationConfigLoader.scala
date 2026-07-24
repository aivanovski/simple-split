package com.github.ai.split.domain

import com.github.ai.split.data.db.DatabaseConfig
import com.github.ai.split.entity.exception.{DomainError, EnvironmentError}
import com.github.ai.split.entity.{ApplicationConfig, HttpProtocol, ServerConfig}
import io.github.cdimascio.dotenv.Dotenv
import zio.*
import zio.direct.*

class ApplicationConfigLoader {

  def loadConfig(): IO[DomainError, ApplicationConfig] = defer {
    val dotenv = Dotenv.configure().ignoreIfMissing().systemProperties().load()

    val protocol = readRequired(dotenv, "PROTOCOL")
      .flatMap(parseProtocol)
      .run
    val serverCertificatePath = readRequired(dotenv, "SERVER_CERTIFICATE_PATH").run
    val serverPrivateKeyPath = readRequired(dotenv, "SERVER_PRIVATE_KEY_PATH").run
    val databaseUrl = readRequired(dotenv, "DATABASE_URL").run
    val databaseMaximumPoolSize = readIntRequired(dotenv, "DATABASE_MAX_POOL_SIZE").run
    val databaseMinimumIdle = readIntRequired(dotenv, "DATABASE_MIN_IDLE").run
    val populateTestData = readBooleanRequired(dotenv, "POPULATE_TEST_DATA").run

    ApplicationConfig(
      server = ServerConfig(
        protocol = protocol,
        certificatePath = serverCertificatePath,
        privateKeyPath = serverPrivateKeyPath
      ),
      database = DatabaseConfig(
        url = databaseUrl,
        maximumPoolSize = databaseMaximumPoolSize,
        minimumIdle = databaseMinimumIdle
      ),
      populateTestData = populateTestData
    )
  }

  private def readOptional(dotenv: Dotenv, key: String): UIO[Option[String]] =
    ZIO.succeed(Option(dotenv.get(key)).map(_.trim).filter(_.nonEmpty))

  private def readRequired(dotenv: Dotenv, key: String): IO[EnvironmentError, String] =
    readOptional(dotenv, key)
      .flatMap(ZIO.fromOption(_))
      .mapError(_ => EnvironmentError(s"Missing required .env value: $key"))

  private def readIntRequired(dotenv: Dotenv, key: String): IO[EnvironmentError, Int] = defer {
    val value = readRequired(dotenv, key).run
    ZIO
      .fromOption(value.toIntOption)
      .mapError(_ => EnvironmentError(s"Invalid integer value in .env variable: $key=$value"))
      .run
  }

  private def readBooleanRequired(dotenv: Dotenv, key: String): IO[EnvironmentError, Boolean] = defer {
    val value = readRequired(dotenv, key).run
    value.toLowerCase match {
      case "true" => true
      case "false" => false
      case _ =>
        ZIO
          .fail(EnvironmentError(s"Invalid boolean value in .env variable: $key=$value"))
          .run
    }
  }

  private def parseProtocol(value: String): IO[EnvironmentError, HttpProtocol] =
    ZIO
      .fromOption(HttpProtocol.fromString(value))
      .mapError(_ => EnvironmentError(s"Invalid PROTOCOL '$value'. Expected 'http' or 'https'"))
}
