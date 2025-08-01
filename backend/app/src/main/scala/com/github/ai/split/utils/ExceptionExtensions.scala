package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError

import java.io.{PrintWriter, StringWriter}
import java.sql.SQLException

extension (exception: Throwable)
  def stackTraceToString(): String = {
    val writer = new StringWriter()
    exception.printStackTrace(new PrintWriter(writer))
    writer.toString
  }

extension (exception: Throwable)
  def toDomainError(): DomainError = {
    DomainError(
      message = exception.getMessage.some,
      cause = exception.some
    )
  }