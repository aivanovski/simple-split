package com.github.ai.split.utils

import com.github.ai.split.entity.exception.DomainError
import zio.{IO, ZIO}

import java.util.UUID

object UuidUtils {
  val EMPTY_UID = new UUID(0, 0)
}

extension (str: String)

  def parseUid(): IO[DomainError, UUID] = {
    ZIO.attempt(UUID.fromString(str))
      .mapError(error => new DomainError(message = "Invalid UUID".some, cause = error.some))
  }

end extension

extension (uuid: UUID)

  def isEmpty(): Boolean =
    uuid.getMostSignificantBits == 0L && uuid.getLeastSignificantBits == 0L

end extension