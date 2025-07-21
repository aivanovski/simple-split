package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import arrow.core.Either
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto

class GroupDetailsInteractor(
    private val repository: GroupRepository
) {

    suspend fun getGroup(
        groupUid: String,
        password: String
    ): Either<AppException, GroupDto> =
        repository.getGroups(
            uids = listOf(groupUid),
            passwords = listOf(password)
        )
            // TODO: handle error if group not found
            .map { groups -> groups.first() }
}