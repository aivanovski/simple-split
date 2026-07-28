package com.github.ai.split.client

import com.github.ai.split.api.{NewExpenseDto, UserNameDto, UserUidDto}
import com.github.ai.split.api.request.{
  LoginRequest,
  PostExpenseRequest,
  PostGroupRequest,
  PostMemberRequest,
  PutMemberRequest
}
import com.github.ai.split.api.response.LoginResponse
import com.github.ai.split.openapi.Schemas.given
import zio.*
import zio.direct.*
import zio.http.*
import zio.schema.Schema
import zio.schema.codec.JsonCodec

class ApiClient(
  private val client: Client
) {

  type ApiResponse = ZIO[Scope, Throwable, Response]

  private val baseUrl = "https://127.0.0.1:8443"

  def login(email: String, password: String) = {
    val body = LoginRequest(email = email, password = password)

    client
      .request(
        Request.post(
          path = s"$baseUrl/login",
          body = Body.fromString(encodeToJson(body))
        )
      )
  }

  def getAuthToken(
    email: String = DefaultUser.Email,
    password: String = DefaultUser.Password
  ): ZIO[Scope, Throwable, String] = {
    login(
      email = email,
      password = password
    )
      .flatMap(_.body.asString)
      .flatMap(jsonResponse => decodeFromJson[LoginResponse](jsonResponse))
      .map(response => response.token)
  }

  def getGroup(
    authToken: String,
    uid: String = Groups.TripToDisneyLand,
    password: String = DefaultUser.Password
  ) = client.request(
    Request(
      method = Method.GET,
      url = URL.decode(s"$baseUrl/group?ids=$uid&passwords=$password").getOrElse(URL.empty),
      headers = Headers(Header.Authorization.Bearer(authToken))
    )
  )

  def getCurrencies(): ApiResponse = {
    client.request(
      Request.get(
        path = s"$baseUrl/currency"
      )
    )
  }

  def postGroup(): ApiResponse = {
    val body = PostGroupRequest(
      DefaultUser.Password,
      "Oktoberfest",
      "Amazing party",
      "USD",
      List("Bob", "Alan").map(UserNameDto(_)),
      List(
        NewExpenseDto(
          "Traditional Beer & Pretzels",
          "Authentic Bavarian beer and pretzels at Oktoberfest",
          45.50,
          List(UserNameDto("Bob")),
          Some(true),
          List.empty
        ),
        // Option 2: Entry tickets
        NewExpenseDto(
          "Oktoberfest Entry Tickets",
          "Entry tickets for the beer festival",
          24.00,
          List(UserNameDto("Alan")),
          Some(true),
          List.empty
        ),

        // Option 3: Traditional food
        NewExpenseDto(
          "Bratwurst and Sauerkraut",
          "Traditional Bavarian sausages and sauerkraut",
          32.75,
          List(UserNameDto("Bob")),
          Some(true),
          List.empty
        )
      )
    )

    client.request(
      Request.post(
        path = s"$baseUrl/group",
        body = Body.fromString(encodeToJson(body))
      )
    )
  }

  def postExpense(
    password: String = DefaultUser.Password,
    title: String = "Beer"
  ): ApiResponse = {
    val body = PostExpenseRequest(
      Groups.TripToDisneyLand,
      title,
      "",
      18.0,
      List(UserUidDto(Users.Mickey)),
      Some(true),
      List.empty
    )

    client.request(
      Request.post(
        path = s"$baseUrl/expense?password=$password",
        body = Body.fromString(encodeToJson(body))
      )
    )
  }

  def postMember(
    password: String = DefaultUser.Password,
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
        body = Body.fromString(encodeToJson(body))
      )
    )
  }

  def deleteMember(
    memberUid: String,
    password: String = DefaultUser.Password
  ): ApiResponse = {
    client.request(
      Request.delete(
        path = s"$baseUrl/member/$memberUid?password=$password"
      )
    )
  }

  def putMember(
    memberUid: String,
    password: String = DefaultUser.Password,
    newName: String
  ): ApiResponse = {
    client.request(
      Request.put(
        path = s"$baseUrl/member/$memberUid?password=$password",
        body = Body.fromString(
          encodeToJson(
            PutMemberRequest(newName)
          )
        )
      )
    )
  }

  private def encodeToJson[A](value: A)(using schema: Schema[A]): String =
    JsonCodec.jsonEncoder(schema).encodeJson(value, None).toString

  private def decodeFromJson[A](json: String)(using schema: Schema[A]): IO[Throwable, A] = {
    val res = ZIO
      .fromEither(JsonCodec.jsonDecoder(schema).decodeJson(json))
      .mapError(error => Exception(s"Failed to decode: $error"))

    res
  }

  def deleteExpense(
    expenseUid: String,
    password: String = DefaultUser.Password
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

object DefaultUser {
  val Email = "mickey.mouse@mail.com"
  val Password = "abc123"
}
