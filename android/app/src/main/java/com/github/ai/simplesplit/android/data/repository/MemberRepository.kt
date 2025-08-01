package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.request.PostMemberRequest
import com.github.ai.split.api.response.DeleteMemberResponse
import com.github.ai.split.api.response.PostMemberResponse

class MemberRepository(
    private val api: ApiClient
) {

    suspend fun createMember(
        password: String,
        request: PostMemberRequest
    ): Either<AppException, PostMemberResponse> =
        api.postMember(
            password = password,
            request = request
        )

    suspend fun removeMember(
        memberUid: String,
        password: String
    ): Either<AppException, DeleteMemberResponse> =
        api.removeMember(
            memberUid = memberUid,
            password = password
        )
}