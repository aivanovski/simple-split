package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.MemberEntityDao
import com.github.ai.split.entity.NewUser
import com.github.ai.split.entity.db.{MemberEntity, MemberUid}
import com.github.ai.split.entity.exception.DomainError
import zio.*

import java.util.UUID

class AddUserUseCase(
  userDao: MemberEntityDao
) {

  def addUser(user: NewUser): IO[DomainError, MemberEntity] = {
    for {
      createdUser <- userDao.add(
        MemberEntity(
          uid = MemberUid(UUID.randomUUID()),
          name = user.name
        )
      )
    } yield createdUser
  }
}
