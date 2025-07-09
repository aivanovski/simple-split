package com.github.ai.split.api.request

import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PostGroupRequest(
  password: String,
  title: String,
  description: Option[String]
)

object PostGroupRequest {
  implicit val decoder: JsonDecoder[PostGroupRequest] = DeriveJsonDecoder.gen[PostGroupRequest]
}
