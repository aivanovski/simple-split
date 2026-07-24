package com.github.ai.split.api.response

import com.github.ai.split.api.{CurrencyDto, ExpenseDto, GetGroupErrorDto, GroupDto, UserDto}

final case class SignupResponse(
  token: String,
  refreshToken: String,
  user: UserDto
)

final case class LoginResponse(
  token: String,
  refreshToken: String,
  user: UserDto
)

final case class RefreshTokenResponse(
  token: String,
  refreshToken: String
)

final case class GetCurrenciesResponse(currencies: List[CurrencyDto])

final case class GetGroupsResponse(
  groups: List[GroupDto],
  errors: List[GetGroupErrorDto]
)

final case class PostGroupResponse(group: GroupDto)
final case class PutGroupResponse(group: GroupDto)
final case class PostMemberResponse(group: GroupDto)
final case class PutMemberResponse(group: GroupDto)
final case class DeleteMemberResponse(group: GroupDto)
final case class PostExpenseResponse(expense: ExpenseDto)
final case class PutExpenseResponse(expense: ExpenseDto)
final case class DeleteExpenseResponse(group: GroupDto)
