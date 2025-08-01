package com.github.ai.split.entity

import com.github.ai.split.entity.db.MemberUid

import java.util.UUID

case class Transaction(
  creditor: MemberUid,
  debtor: MemberUid,
  amount: Double
)
