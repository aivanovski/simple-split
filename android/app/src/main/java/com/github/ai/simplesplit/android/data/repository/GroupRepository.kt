package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.request.PutGroupRequest
import com.github.ai.split.api.response.PostGroupResponse
import com.github.ai.split.api.response.PutGroupResponse

class GroupRepository(
    private val api: ApiClient
) {
    suspend fun getGroup(
        uid: String,
        password: String
    ): Either<AppException, GroupDto> =
        api.getGroups(
            uids = listOf(uid),
            passwords = listOf(password)
        )
            .map { response -> response.groups.first() }

    suspend fun getGroups(
        uids: List<String>,
        passwords: List<String>
    ): Either<AppException, List<GroupDto>> =
        api.getGroups(uids = uids, passwords = passwords)
            .map { response -> response.groups }

    suspend fun createGroup(request: PostGroupRequest): Either<AppException, PostGroupResponse> =
        api.postGroup(request = request)

    suspend fun updateGroup(
        uid: String,
        password: String,
        request: PutGroupRequest
    ): Either<AppException, PutGroupResponse> =
        api.putGroup(
            uid = uid,
            password = password,
            request = request
        )
}