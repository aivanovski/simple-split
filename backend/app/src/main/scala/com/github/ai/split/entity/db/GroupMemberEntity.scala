package com.github.ai.split.entity.db

case class GroupMemberEntity(
  uid: MemberUid,
  groupUid: GroupUid,
  userUid: UserUid
)

object GroupMemberEntity {
  inline val TableName = "group_members"
}
