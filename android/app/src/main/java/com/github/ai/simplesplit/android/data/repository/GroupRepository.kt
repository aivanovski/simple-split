package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.response.PostGroupResponse

class GroupRepository(
    private val api: ApiClient
) {

    suspend fun getGroups(
        uids: List<String>,
        passwords: List<String>
    ): Either<AppException, List<GroupDto>> =
        api.getGroups(uids = uids, passwords = passwords)
            .map { response -> response.groups }

    suspend fun createGroup(request: PostGroupRequest): Either<AppException, PostGroupResponse> =
        api.postGroup(request = request)
}