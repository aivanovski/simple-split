package com.github.ai.split

import com.github.ai.split.data.db.dao.{ExpenseEntityDao, GroupEntityDao, GroupMemberEntityDao, PaidByEntityDao, SplitBetweenEntityDao, UserEntityDao}
import com.github.ai.split.domain.{AccessResolverService, AuthService}
import com.github.ai.split.domain.usecases.{AddExpenseUseCase, AddGroupUseCase, AddMemberUseCase, AddUserUseCase, AssembleExpenseUseCase, AssembleGroupResponseUseCase, AssembleGroupsResponseUseCase, CalculateSettlementUseCase, ConvertExpensesToTransactionsUseCase, GetAllUsersUseCase, GetGroupByUidUseCase, GetUserByEmailUseCase}
import com.github.ai.split.presentation.controllers.{ExpenseController, GroupController, LoginController, MemberController, UserController}
import zio.ZLayer

object Layers {

  // Dao's
  val userDao = ZLayer.fromFunction(UserEntityDao(_))
  val groupDao = ZLayer.fromFunction(GroupEntityDao(_))
  val groupMemberDao = ZLayer.fromFunction(GroupMemberEntityDao(_))
  val expenseDao = ZLayer.fromFunction(ExpenseEntityDao(_))
  val paidByDao = ZLayer.fromFunction(PaidByEntityDao(_))
  val splitBetweenDao = ZLayer.fromFunction(SplitBetweenEntityDao(_))

  // Services
  val authService = ZLayer.fromFunction(AuthService(_))
  val accessResolverService = ZLayer.fromFunction(AccessResolverService(_, _, _))

  // Use cases
  val addUserUseCase = ZLayer.fromFunction(AddUserUseCase(_))
  val getAllUsersUseCase = ZLayer.fromFunction(GetAllUsersUseCase(_))
  val getUserByEmailUseCase = ZLayer.fromFunction(GetUserByEmailUseCase(_))
  val addGroupUseCase = ZLayer.fromFunction(AddGroupUseCase(_, _))
  val getGroupByUidUseCase = ZLayer.fromFunction(GetGroupByUidUseCase(_))
  val addMemberUseCase = ZLayer.fromFunction(AddMemberUseCase(_, _, _))
  val addExpenseUseCase = ZLayer.fromFunction(AddExpenseUseCase(_, _, _, _, _, _, _))
  val convertToTransactionsUseCase = ZLayer.succeed(ConvertExpensesToTransactionsUseCase())
  val calculateSettlementUseCase = ZLayer.succeed(CalculateSettlementUseCase())

  // Response use cases
  val assembleGroupResponseUseCase = ZLayer.fromFunction(AssembleGroupResponseUseCase(_, _, _, _, _, _, _, _))
  val assembleGroupsResponseUseCase = ZLayer.fromFunction(AssembleGroupsResponseUseCase(_, _, _, _, _, _, _, _))
  val assembleExpenseUseCase = ZLayer.fromFunction(AssembleExpenseUseCase(_, _, _, _))

  // Controllers
  val userController = ZLayer.fromFunction(UserController(_, _, _))
  val loginController = ZLayer.fromFunction(LoginController(_, _))
  val groupController = ZLayer.fromFunction(GroupController(_, _, _, _))
  val memberController = ZLayer.fromFunction(MemberController(_, _, _, _, _))
  val expenseController = ZLayer.fromFunction(ExpenseController(_, _))
}
