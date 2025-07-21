package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.UserNameDto
import com.github.ai.split.api.request.PostGroupRequest
import com.github.ai.split.api.response.PostGroupResponse

class GroupEditorInteractor(
    private val repository: GroupRepository,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun createGroup(
        password: String,
        title: String,
        description: String,
        members: List<String>
    ): Either<AppException, PostGroupResponse> =
        either {
            val request = PostGroupRequest(
                password = password,
                title = title,
                description = description.ifEmpty { null },
                members = members.map { UserNameDto(name = it) },
                expenses = null
            )

            val response = repository.createGroup(request).bind()

            credentialsRepository.add(
                GroupCredentials(
                    groupUid = response.group.uid,
                    password = password
                )
            )

            response
        }
}