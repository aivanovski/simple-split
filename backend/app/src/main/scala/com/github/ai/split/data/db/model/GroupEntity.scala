package com.github.ai.split.data.db.model

case class GroupEntity(
  uid: GroupUid,
  title: String,
  description: String,
  passwordHash: String,
  currencyIsoCode: String,
  created: Timestamp,
  modified: Timestamp
)
