package com.github.ai.split.utils

import zio.ZIO

extension [T](option: Option[T]) {

  def mapZIO[R, E, B](
    mapper: T => ZIO[R, E, B]
  ): ZIO[R, E, Option[B]] =
    option match {
      case Some(value) => mapper.apply(value).map(Some(_))
      case None => ZIO.succeed(None)
    }
}
