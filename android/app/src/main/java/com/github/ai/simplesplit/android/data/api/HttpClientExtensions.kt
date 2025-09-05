package com.github.ai.simplesplit.android.data.api

import arrow.core.Either
import arrow.core.computations.ResultEffect.bind
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.model.exception.ApiException
import com.github.ai.simplesplit.android.model.exception.InvalidResponseException
import com.github.ai.simplesplit.android.model.exception.NetworkException
import com.github.ai.split.api.ErrorMessageDto
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.delete
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.put
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.io.IOException

enum class RequestType {
    GET,
    POST,
    PUT,
    DELETE
}

suspend inline fun <reified Request, reified Response> HttpClient.sendRequest(
    type: RequestType,
    url: String,
    body: Request? = null,
    jsonSerializer: JsonSerializer
): Either<ApiException, Response> {
    val client = this

    return either {
        val response = try {
            when (type) {
                RequestType.GET -> client.get(url)

                RequestType.POST -> client.post(url) {
                    contentType(ContentType.Application.Json)
                    if (body != null) {
                        setBody(jsonSerializer.serialize(body))
                    }
                }

                RequestType.PUT -> client.put(url) {
                    contentType(ContentType.Application.Json)
                    if (body != null) {
                        setBody(jsonSerializer.serialize(body))
                    }
                }

                RequestType.DELETE -> client.delete(url)
            }
        } catch (exception: IOException) {
            raise(NetworkException(cause = exception))
        }

        val status = response.status
        if (status != HttpStatusCode.OK) {
            val errorText = response.bodyAsText()
            val error = jsonSerializer
                .deserialize<ErrorMessageDto>(errorText)
                .getOrNull()

            raise(
                InvalidResponseException(
                    statusCode = status.value,
                    errorMessage = error
                )
            )
        }

        jsonSerializer.deserialize<Response>(response.bodyAsText())
            .mapLeft { error -> ApiException(cause = error) }
            .bind()

        // Either.catch { response.body<Response>() }
        //     .mapLeft { error -> ApiException(cause = error) }
        //     .bind()
    }
}