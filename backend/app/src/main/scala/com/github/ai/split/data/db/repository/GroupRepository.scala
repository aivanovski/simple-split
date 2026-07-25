package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{CurrencyEntityDao, GroupEntityDao, MemberEntityDao, UserEntityDao}
import com.github.ai.split.data.db.model.GroupUid
import com.github.ai.split.entity.{GroupWithMembers, MemberWithUser}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

import scala.collection.mutable.ListBuffer

class GroupRepository(
  private val memberDao: MemberEntityDao,
  private val userDao: UserEntityDao,
  private val groupDao: GroupEntityDao,
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
  ): IO[DomainError, List[(GroupUid, List[MemberWithUser])]] = {
    defer {
      val uidsAndMembers = ZIO
        .collectAll(
          groupUids
            .map { groupUid =>
              memberDao
                .getByGroupUid(groupUid = groupUid)
                .map(members => (groupUid, members))
            }
        )
        .run

      ZIO
        .foreach(uidsAndMembers) { case (groupUid, members) =>
          resolveMembers(members).map(groupUid -> _)
        }
        .run
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

  def getMembers(groupUid: GroupUid): IO[DomainError, List[MemberWithUser]] = {
    defer {
      val members = memberDao.getByGroupUid(groupUid).run
      resolveMembers(members).run
    }
  }

  private def resolveMembers(
    members: List[com.github.ai.split.data.db.model.MemberEntity]
  ): IO[DomainError, List[MemberWithUser]] =
    defer {
      val users = userDao.getByUids(members.flatMap(_.userUid).distinct).run
      val usersByUid = users.map(user => user.uid -> user).toMap

      members.map { member =>
        val user = member.userUid.flatMap(userUid => usersByUid.get(userUid))

        MemberWithUser(
          member = member,
          user = user
        )
      }
    }
}
