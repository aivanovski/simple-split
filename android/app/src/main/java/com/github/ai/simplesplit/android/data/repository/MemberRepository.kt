package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.DeleteMemberResponse
import com.github.ai.split.api.PostMemberRequest
import com.github.ai.split.api.PostMemberResponse
import com.github.ai.split.api.PutMemberRequest
import com.github.ai.split.api.PutMemberResponse

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

    suspend fun updateMember(
        memberUid: String,
        password: String,
        request: PutMemberRequest
    ): Either<AppException, PutMemberResponse> =
        api.putMember(
            memberUid = memberUid,
            password = password,
            request = request
        )
}