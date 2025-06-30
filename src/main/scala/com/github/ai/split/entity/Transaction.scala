package com.github.ai.split.entity

import java.util.UUID

case class Transaction(
  creditor: UUID,
  debtor: UUID,
  amount: Double
)
