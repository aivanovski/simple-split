package com.github.ai.split.api.request

import com.github.ai.split.api.{NewExpenseDto, UserNameDto, UserUidDto}

final case class PostGroupRequest(
  password: String = "",
  title: String = "",
  description: String = "",
  currencyIsoCode: String = "",
  members: List[UserNameDto] = Nil,
  expenses: List[NewExpenseDto] = Nil
)

final case class PutGroupRequest(
  title: Option[String] = None,
  password: Option[String] = None,
  description: Option[String] = None,
  currencyIsoCode: Option[String] = None,
  members: List[UserUidDto] = Nil
)

final case class PostMemberRequest(
  groupUid: String = "",
  name: String = ""
)

final case class PutMemberRequest(name: String = "")

final case class PostExpenseRequest(
  groupUid: String = "",
  title: String = "",
  description: String = "",
  amount: Double = 0.0,
  paidBy: List[UserUidDto] = Nil,
  isSplitBetweenAll: Option[Boolean] = None,
  splitBetween: List[UserUidDto] = Nil
)

final case class PutExpenseRequest(
  title: Option[String] = None,
  description: Option[String] = None,
  amount: Option[Double] = None,
  paidBy: List[UserUidDto] = Nil,
  isSplitBetweenAll: Option[Boolean] = None,
  splitBetween: List[UserUidDto] = Nil
)
