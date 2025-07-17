package com.github.ai.simplesplit.android.presentation.screens.groups

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.simplesplit.android.utils.asUid
import com.github.ai.split.api.GroupDto
import java.util.UUID

class GroupsInteractor(
    private val groupRepository: GroupRepository,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun getStoredGroups(): Either<AppException, List<GroupDto>> =
        either {
            val storedCredentials = credentialsRepository.getAll()

            if (storedCredentials.isNotEmpty()) {
                val (uids, passwords) = storedCredentials
                    .map { credentials ->
                        credentials.groupUid to credentials.password
                    }
                    .unzip()

                val groups = groupRepository.getGroups(
                    uids = uids,
                    passwords = passwords
                ).bind()

                groups
            } else {
                emptyList()
            }

        }
}