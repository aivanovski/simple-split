package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.*

import scala.io.Source

object Resources {

  def readResourceAsString(path: String): IO[DomainError, String] = {
    ZIO
      .attempt {
        val content = getClass.getResourceAsStream(path)
        Source.fromInputStream(content).mkString
      }
      .mapError(error => DomainError(message = s"Failed to read resource: $path".some, cause = error.some))
  }
}
