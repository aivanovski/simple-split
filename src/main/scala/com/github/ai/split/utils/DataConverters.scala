package com.github.ai.split.utils

import com.github.ai.split.entity.{Expense, Group, User}
import com.github.ai.split.entity.api.{ExpenseDto, GroupDto, UserDto}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

def toUserDto(user: User) = UserDto(
  uid = user.uid.toString,
  email = user.email
)

def toUserDtos(users: List[User]) = users.map { user => toUserDto(user) }

def toGroupDtos(
  groups: List[Group],
  userUidToUserMap: Map[UUID, User]
): IO[DomainError, List[GroupDto]] =
  ZIO.collectAll(
    groups
      .map { group =>
        toGroupDto(
          group = group,
          userUidToUserMap = userUidToUserMap
        )
      }
  )

def toExpenseDto(
  expense: Expense,
  userUidToUserMap: Map[UUID, User]
): IO[DomainError, ExpenseDto] = {
  for
    paidBy <- toUsers(expense.paidBy, userUidToUserMap)
    splitBetween <- toUsers(expense.splitBetween, userUidToUserMap)
  yield
    ExpenseDto(
      uid = expense.uid.toString,
      title = expense.title,
      description = expense.description.some,
      amount = expense.amount,
      paidBy = toUserDtos(paidBy),
      splitBetween = toUserDtos(splitBetween)
    )
}

def toUsers(
  uids: List[UUID],
  userUidToUserMap: Map[UUID, User]
): IO[DomainError, List[User]] = {
  ZIO.collectAll(
    uids.map { uid =>
      ZIO.fromOption(userUidToUserMap.get(uid))
        .mapError(_ => DomainError(message = "User not found".some))
    }
  )
}

def toGroupDto(
  group: Group,
  userUidToUserMap: Map[UUID, User]
): IO[DomainError, GroupDto] = {
  val owner = userUidToUserMap.get(group.ownerUid)
  if (owner.isEmpty) {
    return ZIO.fail(DomainError(message = "User not found".some))
  }

  for {
    members <- toUsers(group.members, userUidToUserMap)

    expenses <- ZIO.collectAll(
      group.expenses.map(expense => toExpenseDto(expense, userUidToUserMap))
    )
  } yield
    GroupDto(
      uid = group.uid.toString,
      owner = toUserDto(owner.get),
      title = group.title,
      description = group.description,
      members = toUserDtos(members),
      expenses = expenses
    )
}