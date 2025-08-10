package com.github.ai.simplesplit.android.domain.usecase

import androidx.core.net.toUri
import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.simplesplit.android.model.exception.ParsingException

class ParseGroupUrlUseCase {

    fun parseUrl(url: String): Either<ParsingException, GroupCredentials> =
        either {
            val uri = Either.catch { url.toUri() }
                .mapLeft { error -> ParsingException(cause = error) }
                .bind()

            // TODO: check that domain is valid

            val ids = uri.getQueryParameter(PARAM_IDS).orEmpty()
            if (ids.isEmpty() || ids.contains(",")) {
                raise(ParsingException("Invalid '$PARAM_IDS' parameter"))
            }

            val passwords = uri.getQueryParameter(PARAM_PASSWORDS).orEmpty()

            GroupCredentials(
                groupUid = ids,
                password = passwords
            )
        }

    companion object {
        private const val PARAM_IDS = "ids"
        private const val PARAM_PASSWORDS = "passwords"
    }
}