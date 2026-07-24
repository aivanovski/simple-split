package com.github.ai.simplesplit.android.data.api

import arrow.core.Either
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.data.settings.Settings
import com.github.ai.simplesplit.android.model.exception.ApiException
import com.github.ai.simplesplit.android.utils.atomicReference
import com.github.ai.split.api.DeleteExpenseResponse
import com.github.ai.split.api.DeleteMemberResponse
import com.github.ai.split.api.GetCurrenciesResponse
import com.github.ai.split.api.GetGroupsResponse
import com.github.ai.split.api.PostExpenseRequest
import com.github.ai.split.api.PostExpenseResponse
import com.github.ai.split.api.PostGroupRequest
import com.github.ai.split.api.PostGroupResponse
import com.github.ai.split.api.PostMemberRequest
import com.github.ai.split.api.PostMemberResponse
import com.github.ai.split.api.PutExpenseRequest
import com.github.ai.split.api.PutExpenseResponse
import com.github.ai.split.api.PutGroupRequest
import com.github.ai.split.api.PutGroupResponse
import com.github.ai.split.api.PutMemberRequest
import com.github.ai.split.api.PutMemberResponse
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
            url = "$baseUrl/group?ids=$idsStr&passwords=$passwordsStr",
            jsonSerializer = jsonSerializer
        )
    }

    suspend fun postGroup(request: PostGroupRequest): Either<ApiException, PostGroupResponse> =
        httpClient.sendRequest<PostGroupRequest, PostGroupResponse>(
            type = RequestType.POST,
            url = "$baseUrl/group",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun postExpense(
        password: String,
        request: PostExpenseRequest
    ): Either<ApiException, PostExpenseResponse> =
        httpClient.sendRequest<PostExpenseRequest, PostExpenseResponse>(
            type = RequestType.POST,
            url = "$baseUrl/expense?password=$password",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun putExpense(
        password: String,
        expenseUid: String,
        request: PutExpenseRequest
    ): Either<ApiException, PutExpenseResponse> =
        httpClient.sendRequest(
            type = RequestType.PUT,
            url = "$baseUrl/expense/$expenseUid?password=$password",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun removeExpense(
        password: String,
        expenseUid: String
    ): Either<ApiException, DeleteExpenseResponse> =
        httpClient.sendRequest<Unit, DeleteExpenseResponse>(
            type = RequestType.DELETE,
            url = "$baseUrl/expense/$expenseUid?password=$password",
            jsonSerializer = jsonSerializer
        )

    suspend fun putGroup(
        uid: String,
        password: String,
        request: PutGroupRequest
    ): Either<ApiException, PutGroupResponse> =
        httpClient.sendRequest<PutGroupRequest, PutGroupResponse>(
            type = RequestType.PUT,
            url = "$baseUrl/group/$uid?password=$password",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun postMember(
        password: String,
        request: PostMemberRequest
    ): Either<ApiException, PostMemberResponse> =
        httpClient.sendRequest(
            type = RequestType.POST,
            url = "$baseUrl/member?password=$password",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun removeMember(
        memberUid: String,
        password: String
    ): Either<ApiException, DeleteMemberResponse> =
        httpClient.sendRequest<Unit, DeleteMemberResponse>(
            type = RequestType.DELETE,
            url = "$baseUrl/member/$memberUid?password=$password",
            jsonSerializer = jsonSerializer
        )

    suspend fun putMember(
        memberUid: String,
        password: String,
        request: PutMemberRequest
    ): Either<ApiException, PutMemberResponse> =
        httpClient.sendRequest<PutMemberRequest, PutMemberResponse>(
            type = RequestType.PUT,
            url = "$baseUrl/member/$memberUid?password=$password",
            body = request,
            jsonSerializer = jsonSerializer
        )

    suspend fun getCurrencies(): Either<ApiException, GetCurrenciesResponse> =
        httpClient.sendRequest<Unit, GetCurrenciesResponse>(
            type = RequestType.GET,
            url = "$baseUrl/currency",
            jsonSerializer = jsonSerializer
        )

    companion object {
        const val PROD_SERVER_URL = "https://api.simplesplitapp.link"
        const val DEBUG_SERVER_URL = "https://10.0.2.2:8443"
    }
}