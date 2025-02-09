package com.github.ai.split.entity

import com.github.ai.split.utils.UuidUtils
import com.github.ai.split.utils.UuidUtils.EMPTY_UID

import java.util.UUID

case class User(
  uid: UUID = EMPTY_UID,
  email: String,
  password: String
)