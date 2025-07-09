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

extension (sqlException: SQLException)
  def toDomainError(): DomainError = {
    DomainError(
      message = sqlException.getMessage.some,
      cause = sqlException.some
    )
  }