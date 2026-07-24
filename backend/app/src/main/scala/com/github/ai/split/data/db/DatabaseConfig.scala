package com.github.ai.split.data.db

case class DatabaseConfig(
  url: String,
  maximumPoolSize: Int,
  minimumIdle: Int
)
