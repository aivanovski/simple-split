package com.github.ai.split.data.db

import com.github.ai.split.data.db.DatabaseConnectionFactory.{
  POSTGRES_DB,
  POSTGRES_HOST,
  POSTGRES_PASSWORD,
  POSTGRES_USER
}
import com.github.ai.split.data.db.model.DatabaseConnection
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.direct.{defer, run}
import zio.{IO, ZIO}

import java.io.File
import scala.io.Source

class DatabaseConnectionFactory {

  def create(): IO[DomainError, DatabaseConnection] = {
    defer {
      val host = sys.env.getOrElse(POSTGRES_HOST, "")
      val db = sys.env.getOrElse(POSTGRES_DB, "")
      val user = sys.env.getOrElse(POSTGRES_USER, "")
      val password = sys.env.getOrElse(POSTGRES_PASSWORD, "")

      val envFileContent = readEnvironmentFile()
        .flatMap { content => parseEnvironmentFile(content) }

      if (host.nonEmpty && db.nonEmpty && user.nonEmpty) {
        newConnectionFrom(
          host = host,
          database = db,
          user = user,
          password = password
        )
      } else if (envFileContent.isDefined) {
        envFileContent.get
      } else {
        ZIO.fail(DomainError(message = "Database connection is not specified".some)).run
      }
    }
  }

  private def readEnvironmentFile(): Option[String] = {
    val envFile = File(".env")
    if (!envFile.exists()) {
      return None
    }

    val source = Source.fromFile(envFile)
    try {
      Some(source.mkString)
    } finally {
      source.close()
    }
  }

  private def parseEnvironmentFile(
    content: String
  ): Option[DatabaseConnection] = {
    val keyToValueMap = content
      .split("\n")
      .map(line => line.trim)
      .filter(line => line.nonEmpty)
      .flatMap { line =>
        val values = line.split("=").toList
        if (values.size == 2) {
          Some((values.head, values(1)))
        } else {
          None
        }
      }
      .toMap

    val host = keyToValueMap.getOrElse(POSTGRES_HOST, "")
    val db = keyToValueMap.getOrElse(POSTGRES_DB, "")
    val user = keyToValueMap.getOrElse(POSTGRES_USER, "")
    val password = keyToValueMap.getOrElse(POSTGRES_PASSWORD, "")
    if (host.nonEmpty && db.nonEmpty && user.nonEmpty) {
      Some(
        newConnectionFrom(
          host = host,
          database = db,
          user = user,
          password = password
        )
      )
    } else {
      None
    }
  }

  private def newConnectionFrom(
    host: String,
    database: String,
    user: String,
    password: String
  ) = DatabaseConnection(
    url = s"jdbc:postgresql://$host:5432/$database",
    user = user,
    password = password
  )
}

object DatabaseConnectionFactory {
  val POSTGRES_HOST = "POSTGRES_HOST"
  val POSTGRES_DB = "POSTGRES_DB"
  val POSTGRES_USER = "POSTGRES_USER"
  val POSTGRES_PASSWORD = "POSTGRES_PASSWORD"
}
