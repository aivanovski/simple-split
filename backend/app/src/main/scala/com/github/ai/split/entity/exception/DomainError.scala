package com.github.ai.split.entity.exception

import com.github.ai.split.data.db.model.GroupUid

class DomainError(
  val message: Option[String] = None,
  val cause: Option[Throwable] = None
) extends Exception(
      message.orNull,
      cause.orNull
    )

class AccessError(
  message: String
) extends DomainError(
      message = Some(message),
      cause = None
    )

class GroupAccessDeniedError(
  groupUid: GroupUid
) extends AccessError(
  message = s"Unable to access the group: $groupUid"
)

class AuthError(
  message: Option[String] = None,
  cause: Option[Throwable] = None
) extends DomainError(
      message = message,
      cause = cause
    )

class InvalidCredentialsError
    extends AuthError(
      message = Some("Invalid email or password"),
      cause = None
    )

class MissingAuthTokenError
    extends AuthError(
      message = Some(
        "Missing auth token. Expected Cookie: authToken=<token> or Authorization: Bearer <token>"
      ),
      cause = None
    )

class ParsingError(
  message: String,
  cause: Option[Throwable] = None
) extends DomainError(message = Some(message), cause = cause)

class EnvironmentError(
  message: String,
  cause: Option[Throwable] = None
) extends DomainError(message = Some(message), cause = cause)

object EnvironmentError {
  def apply(message: String): EnvironmentError =
    new EnvironmentError(message)
}

class JsonDeserializationError(
  typeOf: Class[?],
  cause: Option[Throwable] = None
) extends ParsingError(
      message = s"Unable to deserialize type: ${typeOf.getTypeName}",
      cause = cause
    )
