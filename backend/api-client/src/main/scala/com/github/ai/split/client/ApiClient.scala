package com.github.ai.split.client

import com.github.ai.split.api.{NewExpenseDto, UserNameDto, UserUidDto}
import com.github.ai.split.api.request.{PostExpenseRequest, PostGroupRequest, PostMemberRequest, PutMemberRequest}
import com.google.gson.GsonBuilder
import zio.*
import zio.http.*

import scala.jdk.CollectionConverters.*

class ApiClient(
  private val client: Client
) {

  type ApiResponse = ZIO[Scope, Throwable, Response]

  private val gson = GsonBuilder().setPrettyPrinting().create()
  private val DefaultPassword = "abc123"
  private val baseUrl = "https://127.0.0.1:8443"

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

  def getCurrencies(): ApiResponse = {
    client.request(
      Request.get(
        path = s"$baseUrl/currency"
      )
    )
  }

  def postGroup(): ApiResponse = {
    val body = PostGroupRequest(
      DefaultPassword,
      "Oktoberfest",
      "Amazing party",
      "USD",
      List("Bob", "Alan").map(UserNameDto(_)).asJava,
      List(
        NewExpenseDto(
          "Traditional Beer & Pretzels",
          "Authentic Bavarian beer and pretzels at Oktoberfest",
          45.50,
          List(UserNameDto("Bob")).asJava,
          true,
          List.empty.asJava
        ),
        // Option 2: Entry tickets
        NewExpenseDto(
          "Oktoberfest Entry Tickets",
          "Entry tickets for the beer festival",
          24.00,
          List(UserNameDto("Alan")).asJava,
          true,
          List.empty.asJava
        ),

        // Option 3: Traditional food
        NewExpenseDto(
          "Bratwurst and Sauerkraut",
          "Traditional Bavarian sausages and sauerkraut",
          32.75,
          List(UserNameDto("Bob")).asJava,
          true,
          List.empty.asJava
        )
      ).asJava
    )

    client.request(
      Request.post(
        path = s"$baseUrl/group",
        body = Body.fromString(gson.toJson(body))
      )
    )
  }

  def postExpense(
    password: String = DefaultPassword,
    title: String = "Beer"
  ): ApiResponse = {
    val body = PostExpenseRequest(
      Groups.TripToDisneyLand,
      title,
      "",
      18.0,
      List(UserUidDto(Users.Mickey)).asJava,
      true,
      List.empty.asJava
    )

    client.request(
      Request.post(
        path = s"$baseUrl/expense?password=$password",
        body = Body.fromString(gson.toJson(body))
      )
    )
  }

  def postMember(
    password: String = DefaultPassword,
    groupUid: String = Groups.TripToDisneyLand,
    userName: String = "Bob"
  ): ApiResponse = {
    val body = PostMemberRequest(
      groupUid,
      userName
    )

    client.request(
      Request.post(
        path = s"$baseUrl/member?password=$password",
        body = Body.fromString(gson.toJson(body))
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

  def putMember(
    memberUid: String,
    password: String = DefaultPassword,
    newName: String
  ): ApiResponse = {
    client.request(
      Request.put(
        path = s"$baseUrl/member/$memberUid?password=$password",
        body = Body.fromString(
          gson.toJson(
            PutMemberRequest(newName)
          )
        )
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

  private object Users {
    val Mickey = "00000000-0000-0000-0000-a00000000001"
    val Donald = "00000000-0000-0000-0000-a00000000002"
  }
}

object Groups {
  val TripToDisneyLand = "00000000-0000-0000-0000-b00000000001"
}
