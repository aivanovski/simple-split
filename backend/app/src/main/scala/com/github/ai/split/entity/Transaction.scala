package com.github.ai.split.entity

import com.github.ai.split.entity.db.MembershipUid

import java.util.UUID

case class Transaction(
  creditor: MembershipUid,
  debtor: MembershipUid,
  amount: Double
)
