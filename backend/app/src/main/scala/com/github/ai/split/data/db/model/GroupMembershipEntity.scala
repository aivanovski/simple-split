package com.github.ai.split.data.db.model

case class GroupMembershipEntity(
  uid: MembershipUid,
  groupUid: GroupUid,
  memberUid: MemberUid
)
