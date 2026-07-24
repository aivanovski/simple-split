package com.github.ai.split.entity

import com.github.ai.split.data.db.DatabaseConfig

case class ApplicationConfig(
  server: ServerConfig,
  database: DatabaseConfig,
  populateTestData: Boolean
)

case class ServerConfig(
  protocol: HttpProtocol,
  certificatePath: String,
  privateKeyPath: String
)
