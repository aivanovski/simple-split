package com.github.ai.split.entity

case class ErrorResponse(
  message: Option[String] = None,
  cause: Option[Exception] = None
)

object ErrorResponse {

  def apply(message: String): ErrorResponse =
    ErrorResponse(message = Some(message))

  def apply(cause: Exception): ErrorResponse =
    ErrorResponse(cause = Some(cause))
}
