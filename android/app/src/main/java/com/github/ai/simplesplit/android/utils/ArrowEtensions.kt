package com.github.ai.simplesplit.android.utils

import arrow.core.Either
import com.github.ai.simplesplit.android.model.exception.AppException

fun Either<AppException, Any>.getErrorMessage(): String {
    // TODO: get root cause
    return leftOrNull()?.message ?: "Error has been occurred"
}