package com.github.ai.split.entity

import com.github.ai.split.data.db.model.MembershipUid

sealed trait UserReference

case class MemberReference(
  uid: MembershipUid
) extends UserReference

case class NameReference(
  name: String
) extends UserReference
