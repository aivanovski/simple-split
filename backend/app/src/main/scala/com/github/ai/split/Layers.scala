package com.github.ai.split

import com.github.ai.split.data.db.dao.{
  ExpenseEntityDao,
  GroupEntityDao,
  GroupMemberEntityDao,
  PaidByEntityDao,
  SplitBetweenEntityDao,
  UserEntityDao
}
import com.github.ai.split.data.db.repository.{ExpenseRepository, GroupRepository}
import com.github.ai.split.domain.{AccessResolverService, AuthService, PasswordService}
import com.github.ai.split.domain.usecases.{
  AddExpenseUseCase,
  AddGroupUseCase,
  AddMembersUseCase,
  AddUserUseCase,
  AssembleExpenseUseCase,
  AssembleGroupResponseUseCase,
  AssembleGroupsResponseUseCase,
  CalculateSettlementUseCase,
  ConvertExpensesToTransactionsUseCase,
  FillTestDataUseCase,
  GetAllUsersUseCase,
  GetGroupUseCase,
  RemoveExpenseUseCase,
  RemoveMembersUseCase,
  ResolveUserReferencesUseCase,
  UpdateExpenseUseCase,
  UpdateGroupUseCase,
  ValidateExpenseUseCase,
  ValidateMemberNameUseCase
}
import com.github.ai.split.presentation.controllers.{ExpenseController, GroupController, MemberController}
import zio.{ZIO, ZLayer}

object Layers {

  // Dao's
  val userDao = ZLayer.fromFunction(UserEntityDao(_))
  val groupDao = ZLayer.fromFunction(GroupEntityDao(_))
  val groupMemberDao = ZLayer.fromFunction(GroupMemberEntityDao(_))
  val expenseDao = ZLayer.fromFunction(ExpenseEntityDao(_))
  val paidByDao = ZLayer.fromFunction(PaidByEntityDao(_))
  val splitBetweenDao = ZLayer.fromFunction(SplitBetweenEntityDao(_))

  // Repositories
  val expenseRepository = ZLayer.fromFunction(ExpenseRepository(_, _, _))
  val groupRepository = ZLayer.fromFunction(GroupRepository(_, _, _))

  // Services
  val passwordService = ZLayer.succeed(PasswordService())
  val accessResolverService = ZLayer.fromFunction(AccessResolverService(_, _, _, _))

  // Use cases
  val addUserUseCase = ZLayer.fromFunction(AddUserUseCase(_))
  val getAllUsersUseCase = ZLayer.fromFunction(GetAllUsersUseCase(_))
  val addGroupUseCase = ZLayer.fromFunction(AddGroupUseCase(_, _, _, _, _, _, _))
  val getGroupByUidUseCase = ZLayer.fromFunction(GetGroupUseCase(_, _))
  val addMemberUseCase = ZLayer.fromFunction(AddMembersUseCase(_, _, _, _, _))
  val addExpenseUseCase = ZLayer.fromFunction(AddExpenseUseCase(_, _, _, _, _, _, _))
  val convertToTransactionsUseCase = ZLayer.succeed(ConvertExpensesToTransactionsUseCase())
  val calculateSettlementUseCase = ZLayer.succeed(CalculateSettlementUseCase())
  val fillTestDataUseCase = ZLayer.fromFunction(FillTestDataUseCase(_, _, _, _, _, _, _, _))
  val updateGroupUseCase = ZLayer.fromFunction(UpdateGroupUseCase(_, _, _, _, _, _, _, _))
  val updateExpenseUseCase = ZLayer.fromFunction(UpdateExpenseUseCase(_, _, _, _, _, _, _))
  val removeMembersUseCase = ZLayer.fromFunction(RemoveMembersUseCase(_, _, _, _))
  val resolveUserReferencesUseCase = ZLayer.fromFunction(ResolveUserReferencesUseCase(_))
  val validateMemberNameUseCase = ZLayer.fromFunction(ValidateMemberNameUseCase(_))
  val validateExpenseUseCase = ZLayer.fromFunction(ValidateExpenseUseCase(_, _))
  val removeExpenseUseCase = ZLayer.fromFunction(RemoveExpenseUseCase(_))

  // Response use cases
  val assembleGroupResponseUseCase = ZLayer.fromFunction(AssembleGroupResponseUseCase(_, _, _, _, _, _))
  val assembleGroupsResponseUseCase = ZLayer.fromFunction(AssembleGroupsResponseUseCase(_, _, _, _, _, _, _, _))
  val assembleExpenseUseCase = ZLayer.fromFunction(AssembleExpenseUseCase(_, _, _))

  // Controllers
  val groupController = ZLayer.fromFunction(GroupController(_, _, _, _, _, _, _, _, _))
  val memberController = ZLayer.fromFunction(MemberController(_, _, _, _, _, _, _))
  val expenseController = ZLayer.fromFunction(ExpenseController(_, _, _, _, _, _, _))
}
