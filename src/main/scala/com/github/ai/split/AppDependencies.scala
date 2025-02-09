package com.github.ai.split

import com.github.ai.split.data.{ExpenseRepository, GroupRepository, UserRepository}
import com.github.ai.split.domain.{AuthService, ExpenseCalculator}
import com.github.ai.split.domain.usecases.AddExpenseUseCase
import com.github.ai.split.presentation.controllers.{ExpenseController, GroupController, LoginController, MemberController, UserController}
import com.github.ai.split.presentation.routes.{ExpenseRoutes, GroupRoutes, LoginRoutes, MemberRoutes, UserRoutes}

object AppDependencies {

  // Repositories
  lazy val userRepository = new UserRepository()
  lazy val groupRepository = new GroupRepository()
  lazy val expenseRepository = new ExpenseRepository(groupRepository)

  // Services
  lazy val authService = new AuthService(userRepository)
  lazy val expenseCalculator = new ExpenseCalculator()

  // Use cases
  lazy val addExpenseUseCase = new AddExpenseUseCase(groupRepository)

  // Controllers
  lazy val userController = new UserController(userRepository, authService)
  lazy val loginController = new LoginController(userRepository, authService)
  lazy val groupController = new GroupController(groupRepository, userRepository)
  lazy val memberController = new MemberController(groupRepository, userRepository)
  lazy val expenseController = new ExpenseController(
    expenseRepository,
    groupRepository,
    userRepository,
    addExpenseUseCase
  )

  // Routes
  lazy val loginRoutes = new LoginRoutes(loginController)
  lazy val userRoutes = new UserRoutes(userController, authService)
  lazy val groupRoutes = new GroupRoutes(groupController, authService)
  lazy val memberRoutes = new MemberRoutes(memberController, authService)
  lazy val expenseRoutes = new ExpenseRoutes(expenseController, authService)
}
