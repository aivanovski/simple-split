package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.dao.GroupEntityDao
import java.util.UUID

class GetGroupByUidUseCase(
  private val groupDao: GroupEntityDao
) {

  def getGroupByUid(uid: UUID) = groupDao.getByUid(uid)
}
