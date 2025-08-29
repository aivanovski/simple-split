package com.github.ai.split.entity

enum HttpProtocol {
  case HTTP, HTTPS
}

object HttpProtocol {
  def fromString(name: String): Option[HttpProtocol] = {
    HttpProtocol.values.find(protocol => protocol.toString.equalsIgnoreCase(name))
  }
}
