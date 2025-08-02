package com.github.ai.split.entity.exception

class DomainError(
  val message: Option[String] = None,
  val cause: Option[Throwable] = None
) extends Exception(
      message.orNull,
      cause.orNull
    )
