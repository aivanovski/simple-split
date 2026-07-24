package com.github.ai.split.data.db

import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import io.github.cdimascio.dotenv.Dotenv
import slick.jdbc.SQLiteProfile.api.*
import zio.direct.*
import zio.{Scope, ZIO}

import java.nio.file.Files
import java.nio.file.Path

class DatabaseConnectionFactory {

  def create(useInMemoryDatabase: Boolean): ZIO[Scope, DomainError, Database] = defer {
    val databaseUrl =
      if (useInMemoryDatabase) {
        "jdbc:sqlite::memory:"
      } else {
        val dotenv = Dotenv.configure().ignoreIfMissing().load()
        sys.env
          .get(DatabaseConnectionFactory.DatabaseUrl)
          .orElse(Option(dotenv.get(DatabaseConnectionFactory.DatabaseUrl)))
          .getOrElse(DatabaseConnectionFactory.DefaultDatabaseUrl)
      }

    val db = ZIO.acquireRelease {
      ZIO
        .attempt {
          if (databaseUrl == DatabaseConnectionFactory.DefaultDatabaseUrl) {
            Files.createDirectories(Path.of("app-data", "db"))
          }

          val hikariConfig = HikariConfig()
          hikariConfig.setJdbcUrl(databaseUrl)
          hikariConfig.setDriverClassName("org.sqlite.JDBC")
          hikariConfig.setMaximumPoolSize(if (useInMemoryDatabase) 1 else 4)
          hikariConfig.setMinimumIdle(1)
          hikariConfig.setConnectionInitSql("PRAGMA foreign_keys = ON")

          val dataSource = HikariDataSource(hikariConfig)
          Database.forDataSource(dataSource, Some(hikariConfig.getMaximumPoolSize))
        }
        .mapError(_.toDomainError())
    }(db => ZIO.attempt(db.close()).orDie).run

    db
  }
}

object DatabaseConnectionFactory {
  val DatabaseUrl = "DATABASE_URL"
  val DefaultDatabaseUrl = "jdbc:sqlite:./app-data/db/simple-split.sqlite?journal_mode=WAL&busy_timeout=30000"
}
