package com.github.ai.split.domain.usecases

import com.github.ai.split.entity.CliArguments
import com.github.ai.split.entity.exception.DomainError
import zio.*
import zio.direct.*

class StartUpServerUseCase(
  private val fillTestDataUseCase: FillTestDataUseCase,
  private val fillCurrencyDataUseCase: FillCurrencyDataUseCase,
  private val cliArguments: CliArguments
) {

  def startUpServer(): IO[DomainError, Unit] = {
    defer {
      if (cliArguments.isPopulateTestData) {
        fillTestDataUseCase.createTestData().run
      }

      fillCurrencyDataUseCase.parseAndFillCurrencyData().run

      ZIO.unit.run
    }
  }
}
