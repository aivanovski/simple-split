package com.github.ai.split.openapi

import com.github.ai.split.api.ErrorMessageDto
import com.github.ai.split.api.request.*
import com.github.ai.split.api.response.*
import com.github.ai.split.openapi.Schemas.given
import zio.http.*
import zio.http.codec.{HttpContentCodec, QueryCodec}
import zio.http.endpoint.Endpoint
import zio.http.endpoint.openapi.{OpenAPI, OpenAPIGen}
import zio.schema.Schema

object ApiEndpoints {
  private val defaultStringCodec =
    HttpContentCodec.fromSchema[String](using Schema[String])

  private given csvStringCodec: HttpContentCodec[String] =
    HttpContentCodec(
      defaultStringCodec.choices.updated(
        MediaType.text.csv,
        defaultStringCodec.choices(MediaType.text.plain)
      )
    )

  private def endpoint[PathInput](route: RoutePattern[PathInput]) =
    Endpoint(route).outError[ErrorMessageDto](Status.BadRequest)

  val getGroups =
    endpoint(Method.GET / "group")
      .query(QueryCodec.query[String]("ids"))
      .query(QueryCodec.query[String]("passwords"))
      .out[GetGroupsResponse]
      .tag("groups")

  val postGroup =
    endpoint(Method.POST / "group")
      .in[PostGroupRequest]
      .out[PostGroupResponse]
      .tag("groups")

  val putGroup =
    endpoint(Method.PUT / "group" / string("groupId"))
      .query(QueryCodec.query[String]("password").optional)
      .in[PutGroupRequest]
      .out[PutGroupResponse]
      .tag("groups")

  val postMember =
    endpoint(Method.POST / "member")
      .query(QueryCodec.query[String]("password").optional)
      .in[PostMemberRequest]
      .out[PostMemberResponse]
      .tag("members")

  val putMember =
    endpoint(Method.PUT / "member" / string("memberId"))
      .query(QueryCodec.query[String]("password").optional)
      .in[PutMemberRequest]
      .out[PutMemberResponse]
      .tag("members")

  val deleteMember =
    endpoint(Method.DELETE / "member" / string("memberId"))
      .query(QueryCodec.query[String]("password").optional)
      .out[DeleteMemberResponse]
      .tag("members")

  val postExpense =
    endpoint(Method.POST / "expense")
      .query(QueryCodec.query[String]("password").optional)
      .in[PostExpenseRequest]
      .out[PostExpenseResponse]
      .tag("expenses")

  val putExpense =
    endpoint(Method.PUT / "expense" / string("expenseId"))
      .query(QueryCodec.query[String]("password").optional)
      .in[PutExpenseRequest]
      .out[PutExpenseResponse]
      .tag("expenses")

  val deleteExpense =
    endpoint(Method.DELETE / "expense" / string("expenseId"))
      .query(QueryCodec.query[String]("password").optional)
      .out[DeleteExpenseResponse]
      .tag("expenses")

  val getCurrencies =
    endpoint(Method.GET / "currency")
      .out[GetCurrenciesResponse]
      .tag("currencies")

  val exportGroup =
    endpoint(Method.GET / "export" / string("groupIdAndExtension"))
      .query(QueryCodec.query[String]("password").optional)
      .out[String](MediaType.text.csv)
      .tag("groups")

  val all = List(
    getGroups,
    postGroup,
    putGroup,
    postMember,
    putMember,
    deleteMember,
    postExpense,
    putExpense,
    deleteExpense,
    getCurrencies,
    exportGroup
  )

  val openApi: OpenAPI =
    OpenAPIGen.fromEndpoints(
      title = "Simple Split API",
      version = "0.1.0",
      endpoints = all
    )
}
