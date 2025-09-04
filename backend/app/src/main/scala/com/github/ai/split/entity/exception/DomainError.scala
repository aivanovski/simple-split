package com.github.ai.split.entity.exception

class DomainError(
  val message: Option[String] = None,
  val cause: Option[Throwable] = None
) extends Exception(
      message.orNull,
      cause.orNull
    )

class ParsingError(
  message: String,
  cause: Option[Throwable] = None
) extends DomainError(message = Some(message), cause = cause)

class JsonDeserializationError(
  typeOf: Class[?],
  cause: Option[Throwable] = None
) extends ParsingError(
      message = s"Unable to deserialize type: ${typeOf.getTypeName}",
      cause = cause
    )
