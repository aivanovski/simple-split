package com.github.ai.split.entity

import java.util.UUID

case class PaymentTransaction(
  payerUid: UUID,
  amount: Double
)
