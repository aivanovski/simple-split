package com.github.ai.split.utils

import com.github.ai.split.entity.api.ErrorMessageDto
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import zio.http.{Response, Status}
import zio.json.*

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
    val response = ErrorMessageDto(
      message = if (hasMessage) exception.message else None,
      stacktrace = stacktrace
    )

    Response.error(
      status = Status.BadRequest,
      message = response.toJsonPretty + "\n"
    )
  }

  @tailrec
  private def getRootCauseOrSelf(error: Throwable): Throwable = {
    if (error.getCause == null) error
    else getRootCauseOrSelf(error.getCause)
  }
}

