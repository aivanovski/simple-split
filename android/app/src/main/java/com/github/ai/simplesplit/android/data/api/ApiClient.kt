package com.github.ai.simplesplit.android.data.api

import arrow.core.Either
import arrow.core.Some
import com.github.ai.simplesplit.android.model.exception.ApiException
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.request.PostMemberRequest
import com.github.ai.split.api.request.PutGroupRequest
import com.github.ai.split.api.response.DeleteMemberResponse
import com.github.ai.split.api.response.GetGroupsResponse
import com.github.ai.split.api.response.PostExpenseResponse
import com.github.ai.split.api.response.PostGroupResponse
import com.github.ai.split.api.response.PostMemberResponse
import com.github.ai.split.api.response.PutGroupResponse
import io.ktor.client.HttpClient

class ApiClient(
    private val httpClient: HttpClient,
    private val baseUrl: String
) {

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
}