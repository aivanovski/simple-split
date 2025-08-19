package com.github.ai.split.utils

import com.github.ai.split.api.ErrorMessageDto
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import zio.http.{Body, Response, Status}
import zio.json.*

import java.nio.charset.StandardCharsets.UTF_8
import java.nio.charset.{Charset, StandardCharsets}
import java.util.Base64
import scala.annotation.tailrec

extension (exception: DomainError) {
  def toDomainResponse: Response = {
    val hasMessage = exception.message.isDefined
    val hasCause = exception.cause.isDefined

    val exceptionToPrint = if (exception.cause.isDefined) {
      val cause = exception.cause.get
      getRootCauseOrSelf(cause)
    } else {
      exception
    }

    val stacktrace = exceptionToPrint.stackTraceToString()
    val encodedStacktrace = Base64.getEncoder.encodeToString(stacktrace.getBytes(UTF_8))
    val stacktraceLines = stacktrace
      .split("\n")
      .map(_.replaceAll("\t", "  "))
      .toList

    val response = ErrorMessageDto(
      message = if (hasMessage) exception.message.map(_.trim) else None,
      exception = exceptionToPrint.toString.trim,
      stacktraceBase64 = encodedStacktrace,
      stacktraceLines = stacktraceLines
    )

    Response.error(
      status = Status.BadRequest,
      body = Body.fromString(response.toJsonPretty, UTF_8)
    )
  }

  @tailrec
  private def getRootCauseOrSelf(error: Throwable): Throwable = {
    if (error.getCause == null) error
    else getRootCauseOrSelf(error.getCause)
  }
}
