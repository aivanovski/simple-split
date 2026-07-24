package com.github.ai.split.entity.db

case class GroupEntity(
  uid: GroupUid,
  title: String,
  description: String,
  passwordHash: String,
  currencyIsoCode: String,
  created: Timestamp,
  modified: Timestamp
)
