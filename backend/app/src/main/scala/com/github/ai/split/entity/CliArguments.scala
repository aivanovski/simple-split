package com.github.ai.split.entity

case class CliArguments(
  isUseInMemoryDatabase: Boolean = false,
  isPopulateTestData: Boolean = false
) {

  def toReadableString(): String =
    s"${classOf[CliArguments]}(IN_MEMORY_DB=${isUseInMemoryDatabase}, POPULATE_DATA=${isPopulateTestData})"
}
