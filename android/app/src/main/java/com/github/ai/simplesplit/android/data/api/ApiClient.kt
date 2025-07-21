package com.github.ai.simplesplit.android.data.api

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.model.exception.ApiException
import com.github.ai.simplesplit.android.model.exception.InvalidResponseException
import com.github.ai.simplesplit.android.model.exception.NetworkException
import com.github.ai.split.api.ErrorMessageDto
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.response.GetGroupsResponse
import com.github.ai.split.api.response.PostExpenseResponse
import com.github.ai.split.api.response.PostGroupResponse
import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.request.post
import io.ktor.client.request.setBody
import io.ktor.client.statement.bodyAsText
import io.ktor.http.ContentType
import io.ktor.http.HttpStatusCode
import io.ktor.http.contentType
import java.io.IOException

class ApiClient(
    private val httpClient: HttpClient,
    private val jsonSerializer: JsonSerializer
) {

    suspend fun getGroups(
        uids: List<String>,
        passwords: List<String>
    ): Either<ApiException, GetGroupsResponse> =
        either {
            val idsStr = uids.joinToString(",")
            val passwordsStr = passwords.joinToString(",")
            val url = "http://10.0.2.2:8080/group?ids=$idsStr&passwords=$passwordsStr"

            val response = try {
                httpClient.get(urlString = url)
            } catch (exception: IOException) {
                raise(NetworkException(cause = exception))
            }

            val status = response.status
            if (status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()

                raise(
                    InvalidResponseException(
                        statusCode = status.value,
                        errorMessage = jsonSerializer.deserialize<ErrorMessageDto>(errorBody)
                            .getOrNull()
                    )
                )
            }

            val body = response.bodyAsText()

            jsonSerializer.deserialize<GetGroupsResponse>(body)
                .mapLeft { error -> ApiException(cause = error) }
                .bind()
        }

    suspend fun postGroup(request: PostGroupRequest): Either<ApiException, PostGroupResponse> =
        either {
            val url = "http://10.0.2.2:8080/group"

            val requestBody = jsonSerializer.serialize(request)

            val response = try {
                httpClient.post(urlString = url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
            } catch (exception: IOException) {
                raise(NetworkException(cause = exception))
            }

            val status = response.status
            if (status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()

                raise(
                    InvalidResponseException(
                        statusCode = status.value,
                        errorMessage = jsonSerializer.deserialize<ErrorMessageDto>(errorBody)
                            .getOrNull()
                    )
                )
            }

            val responseBody = response.bodyAsText()

            jsonSerializer.deserialize<PostGroupResponse>(responseBody)
                .mapLeft { error -> ApiException(cause = error) }
                .bind()
        }

    suspend fun postExpense(
        groupUid: String,
        password: String,
        request: PostExpenseRequest
    ): Either<ApiException, PostExpenseResponse> =
        either {
            val url = "http://10.0.2.2:8080/expense/$groupUid?password=$password"

            val requestBody = jsonSerializer.serialize(request)

            val response = try {
                httpClient.post(urlString = url) {
                    contentType(ContentType.Application.Json)
                    setBody(requestBody)
                }
            } catch (exception: IOException) {
                raise(NetworkException(cause = exception))
            }

            val status = response.status
            if (status != HttpStatusCode.OK) {
                val errorBody = response.bodyAsText()

                raise(
                    InvalidResponseException(
                        statusCode = status.value,
                        errorMessage = jsonSerializer.deserialize<ErrorMessageDto>(errorBody)
                            .getOrNull()
                    )
                )
            }

            val responseBody = response.bodyAsText()

            jsonSerializer.deserialize<PostExpenseResponse>(responseBody)
                .mapLeft { error -> ApiException(cause = error) }
                .bind()
        }
}