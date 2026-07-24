package com.github.ai.split.data.db.model

import java.util.UUID

case class SplitBetweenEntity(
  groupUid: GroupUid,
  expenseUid: ExpenseUid,
  membershipUid: MembershipUid
)
