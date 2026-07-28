package com.github.ai.split.data.db.model

case class UserEntity(
  uid: UserUid,
  name: String,
  email: String,
  passwordHash: PasswordHash
)
