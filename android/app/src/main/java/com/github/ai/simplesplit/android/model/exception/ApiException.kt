package com.github.ai.simplesplit.android.model.exception

import com.github.ai.split.api.ErrorMessageDto

open class ApiException(
    message: String? = null,
    cause: Throwable? = null
) : AppException(message, cause)

class NetworkException(
    cause: Throwable
) : ApiException(cause = cause)

class InvalidResponseException(
    statusCode: Int,
    errorMessage: ErrorMessageDto? = null
) : ApiException(
    message = "Invalid server response, HTTP status code: $statusCode"
)