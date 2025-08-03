package com.github.ai.split.entity

case class AccessResolutionResult[T](
  uid: T,
  access: Access,
  reason: Option[Reason]
)

enum Reason {
  case NOT_FOUND
}
