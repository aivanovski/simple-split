package com.github.ai.split.entity

import com.github.ai.split.entity.db.MemberEntity

case class AuthenticationContext(
  user: MemberEntity
)
