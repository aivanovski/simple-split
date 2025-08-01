package com.github.ai.split.entity

import com.github.ai.split.entity.db.MemberUid

sealed trait UserReference

case class MemberReference(
  uid: MemberUid
) extends UserReference

case class NameReference(
  name: String
) extends UserReference
