package com.github.ai.split.utils

import com.github.ai.split.api.ErrorMessageDto
import com.github.ai.split.entity.exception.DomainError
import com.github.ai.split.utils.*
import com.google.gson.GsonBuilder
import zio.http.{Body, Response, Status}

import java.nio.charset.StandardCharsets.UTF_8
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
      if (hasMessage) exception.message.map(_.trim).getOrElse("") else null,
      exceptionToPrint.toString.trim,
      encodedStacktrace,
      stacktraceLines.toJavaList()
    )

    // TODO: use JsonSerializable
    val gson = GsonBuilder().setPrettyPrinting().create()

    Response.error(
      status = Status.BadRequest,
      body = Body.fromString(gson.toJson(gson), UTF_8)
    )
  }

  @tailrec
  private def getRootCauseOrSelf(error: Throwable): Throwable = {
    if (error.getCause == null) error
    else getRootCauseOrSelf(error.getCause)
  }
}
