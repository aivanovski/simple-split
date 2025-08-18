package com.github.ai.split.client

import com.github.ai.split.api.{NewExpenseDto, UserNameDto, UserUidDto}
import com.github.ai.split.api.request.{PostExpenseRequest, PostGroupRequest, PostMemberRequest, PutGroupRequest}
import zio.*
import zio.json.*
import zio.http.*

class ApiClient(
  private val client: Client
) {

  type ApiResponse = ZIO[Scope, Throwable, Response]

  private val DefaultPassword = "abc123"
  private val baseUrl = "https://api.simplesplitapp.link"

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
            expenses = Some(
              List(
                NewExpenseDto(
                  title = "Traditional Beer & Pretzels",
                  description = Some("Authentic Bavarian beer and pretzels at Oktoberfest"),
                  amount = 45.50,
                  paidBy = List(UserNameDto("Bob")),
                  isSplitBetweenAll = Some(true),
                  splitBetween = None
                ),
                // Option 2: Entry tickets
                NewExpenseDto(
                  title = "Oktoberfest Entry Tickets",
                  description = Some("Entry tickets for the beer festival"),
                  amount = 24.00,
                  paidBy = List(UserNameDto("Alan")),
                  isSplitBetweenAll = Some(true),
                  splitBetween = None
                ),

                // Option 3: Traditional food
                NewExpenseDto(
                  title = "Bratwurst and Sauerkraut",
                  description = Some("Traditional Bavarian sausages and sauerkraut"),
                  amount = 32.75,
                  paidBy = List(UserNameDto("Bob")),
                  isSplitBetweenAll = Some(true),
                  splitBetween = None
                )
              )
            )
          ).toJsonPretty
        )
      )
    )
  }

  def postExpense(
    password: String = DefaultPassword,
    title: String = "Beer"
  ): ApiResponse = {
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
  }

  def postMember(
    password: String = DefaultPassword,
    groupUid: String = Groups.TripToDisneyLand,
    userName: String = "Bob"
  ): ApiResponse = {
    client.request(
      Request.post(
        path = s"$baseUrl/member?password=$password",
        body = Body.fromString(
          PostMemberRequest(
            groupUid = groupUid,
            name = userName
          ).toJsonPretty
        )
      )
    )
  }

  def deleteMember(
    memberUid: String,
    password: String = DefaultPassword
  ): ApiResponse = {
    client.request(
      Request.delete(
        path = s"$baseUrl/member/$memberUid?password=$password"
      )
    )
  }

  def deleteExpense(
    expenseUid: String,
    password: String = DefaultPassword
  ): ApiResponse = {
    client.request(
      Request.delete(
        path = s"$baseUrl/expense/$expenseUid?password=$password"
      )
    )
  }

  def updateGroup(
    groupUid: String,
    title: Option[String] = None,
    password: Option[String] = None,
    description: Option[String] = None,
    currentPassword: String = DefaultPassword
  ): ApiResponse = {
    val body = PutGroupRequest(
      title = title,
      password = password,
      description = description,
      members = None
    ).toJsonPretty

    for {
      _ <- Console.printLine(s"Body: $body")
      response <- client.request(
        Request.put(
          path = s"$baseUrl/group/$groupUid?password=$currentPassword",
          body = Body.fromString(body)
        )
      )
    } yield response
  }

  private object Users {
    val Mickey = "00000000-0000-0000-0000-a00000000001"
    val Donald = "00000000-0000-0000-0000-a00000000002"
  }
}

object Groups {
  val TripToDisneyLand = "00000000-0000-0000-0000-b00000000001"
}
