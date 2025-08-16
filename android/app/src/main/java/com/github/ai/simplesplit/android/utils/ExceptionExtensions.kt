package com.github.ai.simplesplit.android.utils

import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.simplesplit.android.model.exception.InvalidResponseException
import com.github.ai.simplesplit.android.model.exception.NetworkException
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider

fun Throwable.getRootCause(): Throwable? {
    var result: Throwable? = this.cause

    while (result?.cause != null) {
        val cause = result.cause
        requireNotNull(cause)
        result = cause
    }

    return result
}

fun Throwable?.hasMessage(): Boolean = this?.message?.isNotBlank() == true

fun AppException.toErrorMessage(resources: ResourceProvider): ErrorMessage {
    return ErrorMessage(
        message = this.formatReadableMessage(resources),
        actionText = StringUtils.EMPTY,
        actionId = null
    )
}

fun AppException.formatReadableMessage(resources: ResourceProvider): String {
    val error = this
    val cause = getRootCause()

    return when {
        error is NetworkException -> buildString {
            append(resources.getString(R.string.network_error_message))

            if (cause.hasMessage()) {
                append(": ")
                append(cause?.message)
            }
        }

        error is InvalidResponseException -> buildString {
            append(resources.getString(R.string.invalid_server_response))

            append(". ")
            if (error.errorMessage?.message?.isNotBlank() == true) {
                append(error.errorMessage.message)
            } else {
                append(resources.getString(R.string.http_status_code_with_str, error.statusCode))
            }
        }

        cause.hasMessage() -> cause?.message ?: StringUtils.EMPTY
        error.hasMessage() -> error.message ?: StringUtils.EMPTY
        else -> resources.getString(R.string.unknown_error_message)
    }
}