package com.github.ai.split.entity.db

import com.github.ai.split.utils.UuidUtils.EMPTY_UID

import java.util.UUID

case class GroupEntity(
  uid: UUID = EMPTY_UID,
  title: String,
  description: String,
  passwordHash: Option[String]
)

object GroupEntity {
  inline val TableName = "groups"
}
