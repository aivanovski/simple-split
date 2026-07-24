package com.github.ai.split.api.response

import com.github.ai.split.api.{CurrencyDto, ExpenseDto, GetGroupErrorDto, GroupDto}

final case class GetCurrenciesResponse(currencies: List[CurrencyDto] = Nil)

final case class GetGroupsResponse(
  groups: List[GroupDto] = Nil,
  errors: List[GetGroupErrorDto] = Nil
)

final case class PostGroupResponse(group: GroupDto = GroupDto())
final case class PutGroupResponse(group: GroupDto = GroupDto())
final case class PostMemberResponse(group: GroupDto = GroupDto())
final case class PutMemberResponse(group: GroupDto = GroupDto())
final case class DeleteMemberResponse(group: GroupDto = GroupDto())
final case class PostExpenseResponse(expense: ExpenseDto = ExpenseDto())
final case class PutExpenseResponse(expense: ExpenseDto = ExpenseDto())
final case class DeleteExpenseResponse(group: GroupDto = GroupDto())
