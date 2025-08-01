package com.github.ai.split.entity.db

import java.util.UUID

case class PaidByEntity(
  groupUid: GroupUid,
  expenseUid: ExpenseUid,
  memberUid: MemberUid
)
