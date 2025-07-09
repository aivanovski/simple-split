package com.github.ai.split.presentation.controllers

import com.github.ai.split.domain.AccessResolverService
import com.github.ai.split.domain.usecases.{AddGroupUseCase, AssembleGroupResponseUseCase, AssembleGroupsResponseUseCase, GetAllUsersUseCase}
import com.github.ai.split.entity.NewGroup
import com.github.ai.split.api.GroupDto
import com.github.ai.split.api.request.{PostGroupRequest, PostUserRequest}
import com.github.ai.split.api.response.GetGroupsResponse
import com.github.ai.split.entity.db.{GroupEntity, UserEntity}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.{asUid, getLastUrlParameter, parse, some}
import zio.{IO, ZIO}
import zio.http.{Request, Response}
import zio.json.*

import java.util.UUID

class GroupController(
  private val accessResolver: AccessResolverService,
  private val addGroupUseCase: AddGroupUseCase,
  private val getAllUsersUseCase: GetAllUsersUseCase,
  private val assembleGroupUseCase: AssembleGroupResponseUseCase,
  private val assembleGroupsUseCase: AssembleGroupsResponseUseCase
) {

  def getGroups(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      groupUids <- parseUids(request)
      passwords <- parsePasswords(request)
      _ <- accessResolver.canAccessToGroups(groupUids = groupUids, passwords = passwords)
      groups <- assembleGroupsUseCase.assembleGroupDtos(uids = groupUids)
    } yield Response.text(GetGroupsResponse(groups).toJsonPretty + "\n")
  }

  def postGroup(
    request: Request
  ): IO[DomainError, Response] = {
    for {
      requestData <- request.body.parse[PostGroupRequest]
      _ <- validateData(requestData)

      group <- addGroupUseCase.addGroup(
        NewGroup(
          password = requestData.password,
          title = requestData.title,
          description = requestData.description.getOrElse("")
        )
      )

      response <- assembleGroupUseCase.assembleGroupDto(groupUid = group.uid)
    } yield Response.text(response.toJsonPretty + "\n")
  }

  private def parseData(request: Request): IO[DomainError, PostGroupRequest] = {
    for {
      data <- request.body.parse[PostGroupRequest]

    } yield data
  }

  private def validateData(data: PostGroupRequest): IO[DomainError, Unit] = {
    if (data.password.isBlank) {
      return ZIO.fail(DomainError(message = "Specified password is empty".some))
    }

    if (data.password.trim.length < 4) {
      return ZIO.fail(DomainError(message = "Specified password is too weak".some))
    }

    // TODO: check group title

    ZIO.succeed(())
  }

  private def parseUids(request: Request): IO[DomainError, List[UUID]] = {
    for {
      uids <- {
        val uids = request.url.queryParamOrElse("ids", "")
          .split(",")
          .toList
          .map(id => id.asUid())

        if (uids.nonEmpty) {
          ZIO.collectAll(uids)
        } else {
          ZIO.fail(DomainError(message = "No group ids were specified".some))
        }
      }
    } yield uids
  }

  private def parsePasswords(request: Request): IO[DomainError, List[String]] = {
    val passwords = request.url.queryParamOrElse("passwords", "")
      .split(",")
      .toList

    ZIO.succeed(passwords)
  }
}
