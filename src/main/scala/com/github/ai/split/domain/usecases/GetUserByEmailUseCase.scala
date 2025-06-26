package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.UserEntityDao

class GetUserByEmailUseCase(
  private val userDao: UserEntityDao
) {

  def getUserByEmail(email: String) = userDao.getByEmail(email)
}
