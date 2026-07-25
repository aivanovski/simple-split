package com.github.ai.split.data.db.model

case class MemberEntity(
  uid: MemberUid,
  groupUid: GroupUid,
  userUid: Option[UserUid],
  name: Option[String],
  email: Option[String],
  acknowledgement: Acknowledgement
)
