package com.github.ai.split.entity

import com.github.ai.split.utils.UuidUtils
import com.github.ai.split.utils.UuidUtils.EMPTY_UID

import java.util.UUID

case class Group(
  uid: UUID = EMPTY_UID,
  ownerUid: UUID,
  title: String,
  description: String,
  members: List[UUID],
  expenses: List[Expense]
)
