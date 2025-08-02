package com.github.ai.split.entity.db

import java.util.UUID

case class UserEntity(
  uid: UserUid,
  name: String
)

object UserEntity {
  inline val TableName = "users"
}
