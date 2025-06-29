package com.github.ai.split.entity.api

import zio.json.{DeriveJsonDecoder, DeriveJsonEncoder, JsonDecoder, JsonEncoder}

case class TransactionDto(
  creditorUid: String,
  debtorUid: String,
  amount: Double
)

object TransactionDto {
  implicit val encoder: JsonEncoder[TransactionDto] = DeriveJsonEncoder.gen[TransactionDto]
  implicit val decoder: JsonDecoder[TransactionDto] = DeriveJsonDecoder.gen[TransactionDto]
}