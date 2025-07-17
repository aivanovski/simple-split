package com.github.ai.simplesplit.android.utils

import arrow.core.Either
import com.github.ai.simplesplit.android.model.exception.ParsingException
import java.util.UUID

fun String.asUid(): Either<ParsingException, UUID> {
    return Either
        .catch {
            UUID.fromString(this)
        }
        .mapLeft { error ->
            ParsingException(
                message = "Invalid UUID format: $this",
                cause = error
            )
        }
}