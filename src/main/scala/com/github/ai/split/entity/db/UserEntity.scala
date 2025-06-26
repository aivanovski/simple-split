package com.github.ai.split.entity.db

import com.github.ai.split.utils.UuidUtils.EMPTY_UID

import java.util.UUID

case class UserEntity(
  uid: UUID = EMPTY_UID,
  email: String,
  password: String // TODO: fix password storing
)