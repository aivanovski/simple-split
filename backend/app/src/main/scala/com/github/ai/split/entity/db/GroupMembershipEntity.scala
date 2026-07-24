package com.github.ai.split.entity.db

case class GroupMembershipEntity(
  uid: MembershipUid,
  groupUid: GroupUid,
  memberUid: MemberUid
)
