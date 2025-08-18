package com.github.ai.split.api.request

import com.github.ai.split.api.UserUidDto
import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class PutGroupRequest(
  title: Option[String],
  password: Option[String],
  description: Option[String],
  members: Option[List[UserUidDto]]
)

object PutGroupRequest {
  implicit val encoder: JsonEncoder[PutGroupRequest] = DeriveJsonEncoder.gen[PutGroupRequest]
  implicit val decoder: JsonDecoder[PutGroupRequest] = DeriveJsonDecoder.gen[PutGroupRequest]
}
