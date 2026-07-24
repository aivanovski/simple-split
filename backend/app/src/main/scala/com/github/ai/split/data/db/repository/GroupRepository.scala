package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{CurrencyEntityDao, GroupEntityDao, GroupMembershipEntityDao, MemberEntityDao}
import com.github.ai.split.data.db.model.GroupUid
import com.github.ai.split.entity.{GroupWithMembers, Member}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import scala.collection.mutable.ListBuffer

class GroupRepository(
  private val memberDao: MemberEntityDao,
  private val groupDao: GroupEntityDao,
  private val groupMembershipDao: GroupMembershipEntityDao,
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
              groupMembershipDao
                .getByGroupUid(groupUid = groupUid)
                .map(members => (groupUid, members))
            }
        )
        .run

      val memberUids = uidsAndMembers
        .flatMap((_, members) => members.map(_.memberUid))
        .distinct

      val userUidToUserMap = memberDao
        .getByUids(memberUids)
        .run
        .map(user => (user.uid, user))
        .toMap

      uidsAndMembers.map { (groupUid, members) =>
        val membersWithUsers = members.map { member =>
          Member(
            user = userUidToUserMap(member.memberUid),
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
      val userUidToUserMap = memberDao
        .getByGroupUid(groupUid)
        .run
        .map(user => (user.uid, user))
        .toMap

      val members = groupMembershipDao.getByGroupUid(groupUid).run

      members
        .map { member =>
          userUidToUserMap
            .get(member.memberUid)
            .map(user => Member(user = user, entity = member))
        }
        .filter(member => member.isDefined)
        .map(member => member.get)
    }
  }
}
