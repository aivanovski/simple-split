package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.{CurrencyEntityDao, GroupEntityDao}
import com.github.ai.split.entity.db.{CurrencyEntity, GroupUid}
import com.github.ai.split.entity.exception.DomainError
import zio.{IO, *}
import zio.direct.*

class CurrencyRepository(
  private val currencyDao: CurrencyEntityDao,
  private val groupDao: GroupEntityDao
) {

  def getAll(): IO[DomainError, List[CurrencyEntity]] =
    currencyDao.getAll()

  def add(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] =
    currencyDao.add(currency)

  def update(currency: CurrencyEntity): IO[DomainError, CurrencyEntity] =
    currencyDao.update(currency)

  def getByIsoCode(isoCode: String): IO[DomainError, CurrencyEntity] =
    currencyDao.getByIsoCode(isoCode)

  def getByGroupUid(groupUid: GroupUid): IO[DomainError, CurrencyEntity] =
    defer {
      val group = groupDao.getByUid(groupUid).run
      currencyDao.getByIsoCode(group.currencyIsoCode).run
    }
}
