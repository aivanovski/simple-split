package com.github.ai.split.openapi

import com.github.ai.split.api.*
import com.github.ai.split.api.request.*
import com.github.ai.split.api.response.*
import zio.schema.{DeriveSchema, Schema}

object Schemas {
  given Schema[CurrencyDto] = DeriveSchema.gen
  given Schema[TimestampDto] = DeriveSchema.gen
  given Schema[MemberDto] = DeriveSchema.gen
  given Schema[UserNameDto] = DeriveSchema.gen
  given Schema[UserUidDto] = DeriveSchema.gen
  given Schema[TransactionDto] = DeriveSchema.gen
  given Schema[ExpenseDto] = DeriveSchema.gen
  given Schema[NewExpenseDto] = DeriveSchema.gen
  given Schema[GroupDto] = DeriveSchema.gen
  given Schema[GetGroupErrorDto] = DeriveSchema.gen
  given Schema[ErrorMessageDto] = DeriveSchema.gen

  given Schema[PostGroupRequest] = DeriveSchema.gen
  given Schema[PutGroupRequest] = DeriveSchema.gen
  given Schema[PostMemberRequest] = DeriveSchema.gen
  given Schema[PutMemberRequest] = DeriveSchema.gen
  given Schema[PostExpenseRequest] = DeriveSchema.gen
  given Schema[PutExpenseRequest] = DeriveSchema.gen

  given Schema[GetCurrenciesResponse] = DeriveSchema.gen
  given Schema[GetGroupsResponse] = DeriveSchema.gen
  given Schema[PostGroupResponse] = DeriveSchema.gen
  given Schema[PutGroupResponse] = DeriveSchema.gen
  given Schema[PostMemberResponse] = DeriveSchema.gen
  given Schema[PutMemberResponse] = DeriveSchema.gen
  given Schema[DeleteMemberResponse] = DeriveSchema.gen
  given Schema[PostExpenseResponse] = DeriveSchema.gen
  given Schema[PutExpenseResponse] = DeriveSchema.gen
  given Schema[DeleteExpenseResponse] = DeriveSchema.gen
}
