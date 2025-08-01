package com.github.ai.split.entity.db

import com.github.ai.split.utils.UuidUtils.EMPTY_UID

case class GroupEntity(
  uid: GroupUid,
  title: String,
  description: String,
  passwordHash: Option[String]
)

object GroupEntity {
  inline val TableName = "groups"
}
