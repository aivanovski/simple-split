package com.github.ai.split.api.request

import com.github.ai.split.api.UserUidDto
import zio.json.{DeriveJsonDecoder, JsonDecoder}

case class PutGroupRequest(
  title: Option[String],
  password: Option[String],
  description: Option[String],
  currencyIsoCode: Option[String],
  members: Option[List[UserUidDto]]
)

object PutGroupRequest {
  implicit val decoder: JsonDecoder[PutGroupRequest] = DeriveJsonDecoder.gen[PutGroupRequest]
}
