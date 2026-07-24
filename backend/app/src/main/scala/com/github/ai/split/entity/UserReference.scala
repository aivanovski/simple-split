package com.github.ai.split.entity

import com.github.ai.split.entity.db.MembershipUid

sealed trait UserReference

case class MemberReference(
  uid: MembershipUid
) extends UserReference

case class NameReference(
  name: String
) extends UserReference
