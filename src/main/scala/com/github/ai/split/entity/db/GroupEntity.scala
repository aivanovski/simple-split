package com.github.ai.split.entity.db

import com.github.ai.split.utils.UuidUtils.EMPTY_UID

import java.util.UUID

case class GroupEntity(
  uid: UUID = EMPTY_UID,
  ownerUid: UUID,
  title: String,
  description: String,
)
