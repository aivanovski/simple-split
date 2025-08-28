package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{CurrencyEntityDao, GroupEntityDao, GroupMemberEntityDao, UserEntityDao}
import com.github.ai.split.entity.db.GroupUid
import com.github.ai.split.entity.{GroupWithMembers, Member}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import scala.collection.mutable.ListBuffer

class GroupRepository(
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val currencyDao: CurrencyEntityDao
) {

  def getByUids(uids: List[GroupUid]): IO[DomainError, List[GroupWithMembers]] = {
    defer {
      val groups = groupDao.getByUids(uids).run

      val currencyIsoCodeToCurrencyMap = currencyDao
        .getByIsoCodes(isoCodes = groups.map(_.currencyIsoCode))
        .run
        .map(currency => (currency.isoCode, currency))
        .toMap

      val groupUidToMembersMap = getMembersByGroupsUids(groupUids = uids).run.toMap

      groups.map { group =>
        GroupWithMembers(
          entity = group,
          currency = currencyIsoCodeToCurrencyMap(group.currencyIsoCode),
          members = groupUidToMembersMap(group.uid)
        )
      }
    }
  }

  private def getMembersByGroupsUids(
    groupUids: List[GroupUid]
  ): IO[DomainError, List[(GroupUid, List[Member])]] = {
    defer {
      val uidsAndMembers = ZIO
        .collectAll(
          groupUids
            .map { groupUid =>
              groupMemberDao
                .getByGroupUid(groupUid = groupUid)
                .map(members => (groupUid, members))
            }
        )
        .run

      val userUids = uidsAndMembers
        .flatMap((_, members) => members.map(_.userUid))
        .distinct

      val userUidToUserMap = userDao
        .getByUids(userUids)
        .run
        .map(user => (user.uid, user))
        .toMap

      uidsAndMembers.map { (groupUid, members) =>
        val membersWithUsers = members.map { member =>
          Member(
            user = userUidToUserMap(member.userUid),
            entity = member
          )
        }

        (groupUid, membersWithUsers)
      }
    }
  }

  def getByUid(groupUid: GroupUid): IO[DomainError, GroupWithMembers] = {
    defer {
      val group = groupDao.getByUid(groupUid).run
      val currency = currencyDao.getByIsoCode(group.currencyIsoCode).run

      GroupWithMembers(
        entity = group,
        currency = currency,
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
