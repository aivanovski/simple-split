package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.db.{GroupEntity, GroupMemberEntity, GroupUid, MemberUid}
import com.github.ai.split.data.db.dao.{GroupEntityDao, GroupMemberEntityDao}
import com.github.ai.split.domain.PasswordService
import com.github.ai.split.entity.{NewExpense, NewGroup}
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.some
import zio.*
import zio.direct.*

import java.util.UUID

class AddGroupUseCase(
  private val passwordService: PasswordService,
  private val groupDao: GroupEntityDao,
  private val groupMemberDao: GroupMemberEntityDao,
  private val addMemberUserCase: AddMembersUseCase,
  private val addExpenseUseCase: AddExpenseUseCase,
  private val validateMemberUseCase: ValidateMemberNameUseCase,
  private val validateExpenseUseCase: ValidateExpenseUseCase
) {

  def addGroup(
    newGroup: NewGroup
  ): IO[DomainError, GroupEntity] = {
    defer {
      validateData(newGroup).run

      val groupUid = GroupUid(UUID.randomUUID())

      val group = groupDao.add(
        GroupEntity(
          uid = groupUid,
          title = newGroup.title,
          description = newGroup.description,
          passwordHash = if (newGroup.password.nonEmpty) {
            passwordService.hashPassword(newGroup.password).some
          } else {
            None
          }
        )
      ).run

      val members = ZIO.collectAll(
        newGroup.members.map { member =>
          addMemberUserCase.addMember(
            groupUid = groupUid,
            name = member.name
          )
        }
      ).run

      val expenses = ZIO.collectAll(
        newGroup.expenses.map { expense =>
          addExpenseUseCase.addExpenseToGroup(
            groupUid = groupUid,
            newExpense = expense
          )
        }
      ).run

      group
    }
  }

  private def validateData(newGroup: NewGroup): IO[DomainError, Unit] = {
    defer {
      if (newGroup.password.isBlank) {
        ZIO.fail(DomainError(message = "Specified password is empty".some)).run
      }

      if (newGroup.password.trim.length < 4) {
        ZIO.fail(DomainError(message = "Specified password is too weak".some)).run
      }

      if (newGroup.title.isBlank) {
        ZIO.fail(DomainError(message = "Group title is empty".some)).run
      }

      if (newGroup.members.nonEmpty) {
        validateMemberUseCase.validateNewMembers(
          currentMemberNames = List.empty,
          newMemberNames = newGroup.members.map(_.name)
        ).run
      }

      if (newGroup.expenses.nonEmpty) {
        // TODO: check expenses have valid names

        ZIO.collectAll(
          newGroup.expenses.map { expense =>
            validateExpenseUseCase.validateExpenseData(
              members = newGroup.members,
              currentExpenses = List.empty,
              expense = expense
            )
          }
        ).run
      }

      ZIO.unit.run
    }
  }
}
