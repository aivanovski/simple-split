package com.github.ai.simplesplit.android.utils

import arrow.core.Either
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider

@Deprecated("Refactor or remove")
fun Either<AppException, Any>.toErrorMessage(resources: ResourceProvider): ErrorMessage {
    val message = leftOrNull()?.formatReadableMessage(resources)
        ?: resources.getString(R.string.unknown_error_message)

    return ErrorMessage(
        message = message,
        actionText = StringUtils.EMPTY,
        actionId = null
    )
}