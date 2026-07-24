package com.github.ai.split.entity

import com.github.ai.split.data.db.DatabaseConfig

case class ApplicationEnvironment(
  server: ServerConfig,
  database: DatabaseConfig,
  jwt: JwtData,
  populateTestData: Boolean
)

case class ServerConfig(
  protocol: HttpProtocol,
  certificatePath: String,
  privateKeyPath: String
)
