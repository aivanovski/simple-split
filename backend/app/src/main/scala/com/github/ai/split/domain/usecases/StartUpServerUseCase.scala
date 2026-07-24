package com.github.ai.split.domain.usecases

import com.github.ai.split.data.db.AppDatabase
import com.github.ai.split.entity.ApplicationEnvironment
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class StartUpServerUseCase(
  private val db: AppDatabase,
  private val fillTestDataUseCase: FillTestDataUseCase,
  private val fillCurrencyDataUseCase: FillCurrencyDataUseCase,
  private val config: ApplicationEnvironment
) {

  def startUpServer(): IO[DomainError, Unit] = {
    defer {
      db.initialize().run

      if (config.populateTestData) {
        fillTestDataUseCase.createTestData().run
      }

      fillCurrencyDataUseCase.parseAndFillCurrencyData().run

      ZIO.unit.run
    }
  }
}
