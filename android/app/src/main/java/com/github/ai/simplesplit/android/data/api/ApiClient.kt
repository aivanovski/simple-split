package com.github.ai.simplesplit.android.data.api

import arrow.core.Either
import arrow.core.Some
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.data.settings.Settings
import com.github.ai.simplesplit.android.model.exception.ApiException
import com.github.ai.simplesplit.android.utils.atomicReference
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.request.PostMemberRequest
import com.github.ai.split.api.request.PutExpenseRequest
import com.github.ai.split.api.request.PutGroupRequest
import com.github.ai.split.api.response.DeleteExpenseResponse
import com.github.ai.split.api.response.DeleteMemberResponse
import com.github.ai.split.api.response.GetGroupsResponse
import com.github.ai.split.api.response.PostExpenseResponse
import com.github.ai.split.api.response.PostGroupResponse
import com.github.ai.split.api.response.PostMemberResponse
import com.github.ai.split.api.response.PutExpenseResponse
import com.github.ai.split.api.response.PutGroupResponse
import io.ktor.client.HttpClient

class ApiClient(
    private val jsonSerializer: JsonSerializer,
    private val settings: Settings
) {

    private var baseUrl by atomicReference(settings.serverUrl)
    private var httpClient by atomicReference(
        buildHttpClient(jsonSerializer, settings)
    )

    fun updateHttpClient() {
        httpClient = buildHttpClient(jsonSerializer, settings)
    }

    fun updateServerUrl() {
        baseUrl = settings.serverUrl
    }

    private fun buildHttpClient(
        jsonSerializer: JsonSerializer,
        settings: Settings
    ): HttpClient {
        return HttpClientFactory.createHttpClient(
            jsonSerializer = jsonSerializer,
            isSslVerificationEnabled = settings.isSslVerificationEnabled,
            logLevel = settings.httpLogLevel
        )
    }

    suspend fun getGroups(
        uids: List<String>,
        passwords: List<String>
    ): Either<ApiException, GetGroupsResponse> {
        val idsStr = uids.joinToString(",")
        val passwordsStr = passwords.joinToString(",")

        return httpClient.sendRequest<Unit, GetGroupsResponse>(
            type = RequestType.GET,
            url = "$baseUrl/group?ids=$idsStr&passwords=$passwordsStr"
        )
    }

    suspend fun postGroup(request: PostGroupRequest): Either<ApiException, PostGroupResponse> =
        httpClient.sendRequest<PostGroupRequest, PostGroupResponse>(
            type = RequestType.POST,
            url = "$baseUrl/group",
            body = Some(request)
        )

    suspend fun postExpense(
        password: String,
        request: PostExpenseRequest
    ): Either<ApiException, PostExpenseResponse> =
        httpClient.sendRequest<PostExpenseRequest, PostExpenseResponse>(
            type = RequestType.POST,
            url = "$baseUrl/expense?password=$password",
            body = Some(request)
        )

    suspend fun putExpense(
        password: String,
        expenseUid: String,
        request: PutExpenseRequest
    ): Either<ApiException, PutExpenseResponse> =
        httpClient.sendRequest(
            type = RequestType.PUT,
            url = "$baseUrl/expense/$expenseUid?password=$password",
            body = Some(request)
        )

    suspend fun removeExpense(
        password: String,
        expenseUid: String
    ): Either<ApiException, DeleteExpenseResponse> =
        httpClient.sendRequest<Unit, DeleteExpenseResponse>(
            type = RequestType.DELETE,
            url = "$baseUrl/expense/$expenseUid?password=$password"
        )

    suspend fun putGroup(
        uid: String,
        password: String,
        request: PutGroupRequest
    ): Either<ApiException, PutGroupResponse> =
        httpClient.sendRequest<PutGroupRequest, PutGroupResponse>(
            type = RequestType.PUT,
            url = "$baseUrl/group/$uid?password=$password",
            body = Some(request)
        )

    suspend fun postMember(
        password: String,
        request: PostMemberRequest
    ): Either<ApiException, PostMemberResponse> =
        httpClient.sendRequest(
            type = RequestType.POST,
            url = "$baseUrl/member?password=$password",
            body = Some(request)
        )

    suspend fun removeMember(
        memberUid: String,
        password: String
    ): Either<ApiException, DeleteMemberResponse> =
        httpClient.sendRequest<Unit, DeleteMemberResponse>(
            type = RequestType.DELETE,
            url = "$baseUrl/member/$memberUid?password=$password"
        )

    companion object {
        const val PROD_SERVER_URL = "https://api.simplesplitapp.link"
        const val DEBUG_SERVER_URL = "http://10.0.2.2:8080"
    }
}