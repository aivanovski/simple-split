package com.github.ai.simplesplit.android.model.exception

open class ParsingException(
    message: String? = null,
    cause: Throwable? = null
) : AppException(message, cause)