package com.github.ai.split.entity

case class JwtData(
  secret: String,
  issuer: String,
  audience: String,
  realm: String
)

object JwtData {
  // TODO: should be read from config
  val DEFAULT = JwtData(
    secret = "secret",
    issuer = "https://0.0.0.0:8443",
    audience = "http://0.0.0.0:8443",
    realm = "SplitIt"
  )
}