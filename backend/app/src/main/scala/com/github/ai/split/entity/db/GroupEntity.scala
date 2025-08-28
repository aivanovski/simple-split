package com.github.ai.split.entity.db

import java.time.LocalDateTime

case class GroupEntity(
  uid: GroupUid,
  title: String,
  description: String,
  passwordHash: Option[String],
  currencyIsoCode: String,
  created: LocalDateTime,
  modified: LocalDateTime
)

object GroupEntity {
  inline val TableName = "groups"
}
