package com.github.ai.simplesplit.android.model.exception

open class AppException(
    message: String? = null,
    cause: Throwable? = null
) : Exception(message, cause)