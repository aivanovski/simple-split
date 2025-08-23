package com.github.ai.split.data.db.repository

import com.github.ai.split.data.db.dao.CurrencyEntityDao
import com.github.ai.split.entity.db.CurrencyEntity

class CurrencyRepository(
  private val dao: CurrencyEntityDao
) {

  def getAll() =
    dao.getAll()

  def add(currency: CurrencyEntity) =
    dao.add(currency)

  def update(currency: CurrencyEntity) =
    dao.update(currency)
}
