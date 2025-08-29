package com.github.ai.split.entity

import com.github.ai.split.entity.HttpProtocol.{HTTP, HTTPS}

case class CliArguments(
  isUseInMemoryDatabase: Boolean,
  isPopulateTestData: Boolean,
  protocol: HttpProtocol
) {

  def getPort(): Int = {
    protocol match
      case HTTP => 8080
      case HTTPS => 8443
  }
}
