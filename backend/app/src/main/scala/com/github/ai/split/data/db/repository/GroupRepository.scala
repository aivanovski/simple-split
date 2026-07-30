package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{CurrencyEntityDao, GroupEntityDao, MemberEntityDao, UserEntityDao}
import com.github.ai.split.data.db.model.Acknowledgement.{CONFIRMED, REQUESTED}
import com.github.ai.split.data.db.model.{GroupUid, UserEntity, UserUid}
import com.github.ai.split.entity.{GroupWithMembers, MemberWithUser}
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

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
              getMembers(groupUid).map(members => (groupUid, members))
            }
        )
        .run

      uidsAndMembers
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
      val users = userDao.getByUids(members.flatMap(_.userUid).distinct).run

      members.zipWithUsers(users)
    }
  }

  def getUserGroupUids(userUid: UserUid): IO[DomainError, List[GroupUid]] = defer {
    val members = memberDao.getAll().run
    val users = userDao.getAll().run

    val user = userDao.getByUid(userUid).run
    val membersAndUsers = members.zipWithUsers(users)

    membersAndUsers
      .filter { member =>
        val isAlreadyMember = member.member.userUid.contains(user.uid)
        val acknowledgement = member.member.acknowledgement

        val wasRequestedToBeMember =
          member.member.email.exists(_.equalsIgnoreCase(user.email)) &&
            (acknowledgement == REQUESTED || acknowledgement == CONFIRMED)

        isAlreadyMember || wasRequestedToBeMember
      }
      .map(_.member.groupUid)
      .distinct
  }
}
