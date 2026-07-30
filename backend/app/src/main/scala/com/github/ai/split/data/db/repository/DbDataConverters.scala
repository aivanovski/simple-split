package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.model.{MemberEntity, UserEntity}
import com.github.ai.split.entity.MemberWithUser

extension (members: List[MemberEntity]) {
  
  def zipWithUsers(users: List[UserEntity]): List[MemberWithUser] = {
    val userUidToUserMap = users.map(user => user.uid -> user).toMap

    members.map { member =>
      val user = member.userUid.flatMap(userUid => userUidToUserMap.get(userUid))

      MemberWithUser(
        member = member,
        user = user
      )
    }
  }
}
