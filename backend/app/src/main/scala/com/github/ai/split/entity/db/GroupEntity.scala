package com.github.ai.split.entity.db

case class GroupEntity(
  uid: GroupUid,
  title: String,
  description: String,
  passwordHash: Option[String]
)

object GroupEntity {
  inline val TableName = "groups"
}
