package com.github.ai.split.entity

import com.github.ai.split.data.db.model.MemberEntity

case class AuthenticationContext(
  user: MemberEntity
)
