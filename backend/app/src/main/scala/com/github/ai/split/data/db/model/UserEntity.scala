package com.github.ai.split.data.db.model

final case class UserEntity(
  uid: UserUid,
  name: String,
  email: String,
  passwordHash: String
)
