package com.github.ai.split.entity

import java.util.UUID

sealed class UserReference

case class UidReference(
  uid: UUID
) extends UserReference

case class NameReference(
  name: String
) extends UserReference
