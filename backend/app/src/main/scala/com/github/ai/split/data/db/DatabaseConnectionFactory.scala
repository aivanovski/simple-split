package com.github.ai.split.data.db

import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.toDomainError
import com.zaxxer.hikari.{HikariConfig, HikariDataSource}
import slick.jdbc.SQLiteProfile.api.*
import zio.direct.*
import zio.{Scope, ZIO}

import java.nio.file.Files
import java.nio.file.Path

class DatabaseConnectionFactory(
  private val config: DatabaseConfig
) {

  def create(): ZIO[Scope, DomainError, Database] = defer {
    val db = ZIO.acquireRelease {
      ZIO
        .attempt {
          Files.createDirectories(Path.of("app-data", "db"))

          val hikariConfig = HikariConfig()
          hikariConfig.setJdbcUrl(config.url)
          hikariConfig.setDriverClassName("org.sqlite.JDBC")
          hikariConfig.setMaximumPoolSize(config.maximumPoolSize)
          hikariConfig.setMinimumIdle(config.minimumIdle)
          hikariConfig.setConnectionInitSql("PRAGMA foreign_keys = ON")

          val dataSource = HikariDataSource(hikariConfig)
          Database.forDataSource(dataSource, Some(config.maximumPoolSize))
        }
        .mapError(_.toDomainError())
    }(db => ZIO.attempt(db.close()).orDie).run

    db
  }
}
