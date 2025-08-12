package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.entity.{GroupWithMembers, Member}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class GroupRepository(
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao
) {

  def getByUid(groupUid: GroupUid): IO[DomainError, GroupWithMembers] = {
    defer {
      val group = groupDao.getByUid(groupUid).run

      GroupWithMembers(
        entity = group,
        members = getMembers(groupUid).run
      )
    }
  }

  def getMembers(groupUid: GroupUid): IO[DomainError, List[Member]] = {
    defer {
      val userUidToUserMap = userDao
        .getByGroupUid(groupUid)
        .run
        .map(user => (user.uid, user))
        .toMap

      val members = groupMemberDao.getByGroupUid(groupUid).run

      members
        .map { member =>
          userUidToUserMap
            .get(member.userUid)
            .map(user => Member(user = user, entity = member))
        }
        .filter(member => member.isDefined)
        .map(member => member.get)
    }
  }
}
