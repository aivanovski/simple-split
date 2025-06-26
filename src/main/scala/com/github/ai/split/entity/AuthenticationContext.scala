package com.github.ai.split.entity

import com.github.ai.split.entity.db.UserEntity

case class AuthenticationContext(
  user: UserEntity
)
