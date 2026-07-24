package com.github.ai.split.entity

import com.github.ai.split.data.db.model.UserEntity

case class AuthenticationContext(
  user: UserEntity
)
