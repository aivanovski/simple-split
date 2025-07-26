package com.github.ai.split.client

import com.github.ai.split.api.{UserNameDto, UserUidDto}
import com.github.ai.split.api.request.{PostExpenseRequest, PostGroupRequest}
import zio.*
import zio.json.*
import zio.http.*

class ApiClient(
  private val client: Client
) {

  type ApiResponse = ZIO[Scope, Throwable, Response]

  private val DefaultPassword = "abc123"
  private val baseUrl = "http://127.0.0.1:8080"

  def getGroup(
    uid: String = Groups.TripToDisneyLand,
    password: String = DefaultPassword
  ): ApiResponse = {
    client.request(
      Request.get(
        path = s"$baseUrl/group?ids=$uid&passwords=$password"
      )
    )
  }

  def postGroup(): ApiResponse = {
    client.request(
      Request.post(
        path = s"$baseUrl/group",
        body = Body.fromString(
          PostGroupRequest(
            password = DefaultPassword,
            title = "Oktoberfest",
            description = Some("Amazing party"),
            members = Some(List("Bob", "Alan").map(UserNameDto(_))),
            expenses = Some(List.empty)
          ).toJsonPretty
        )
      )
    )
  }

  def postExpense(
    password: String = DefaultPassword,
    title: String = "Beer"
  ): ApiResponse =
    client.request(
      Request.post(
        path = s"$baseUrl/expense?password=$password",
        body = Body.fromString(
          PostExpenseRequest(
            groupUid = Groups.TripToDisneyLand,
            title = title,
            description = None,
            amount = 18.0,
            paidBy = List(UserUidDto(Users.Mickey)),
            isSplitBetweenAll = Some(true),
            splitBetween = None
          ).toJsonPretty
        )
      )
    )

  private object Groups {
    val TripToDisneyLand = "00000000-0000-0000-0000-b00000000001"
  }

  private object Users {
    val Mickey = "00000000-0000-0000-0000-a00000000001"
    val Donald = "00000000-0000-0000-0000-a00000000002"
  }
}
