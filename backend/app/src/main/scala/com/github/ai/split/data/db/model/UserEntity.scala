package com.github.ai.split.data.db.model

// TODO: Add PasswordHash type
final case class UserEntity(
  uid: UserUid,
  name: String,
  email: String,
  passwordHash: String
)
