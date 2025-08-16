package com.github.ai.simplesplit.android.presentation.screens.groups

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.domain.usecase.CreateExportUrlUseCase
import com.github.ai.simplesplit.android.domain.usecase.CreateGroupUrlUseCase
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsData
import kotlinx.coroutines.flow.Flow

class GroupsInteractor(
    private val groupRepository: GroupRepository,
    private val credentialsRepository: GroupCredentialsRepository,
    private val exportUrlUseCase: CreateExportUrlUseCase,
    private val groupUrlUseCase: CreateGroupUrlUseCase
) {

    suspend fun loadData(): Either<AppException, GroupsData> =
        either {
            val credentials = credentialsRepository.getAll()

            val groups = if (credentials.isNotEmpty()) {
                val (uids, passwords) = credentials
                    .map { creds -> creds.groupUid to creds.password }
                    .unzip()

                val groups = groupRepository.getGroups(
                    uids = uids,
                    passwords = passwords
                ).bind()

                groups
            } else {
                emptyList()
            }

            GroupsData(
                groups = groups,
                requestedCredentials = credentials
            )
        }

    fun removeGroup(groupUid: String): Either<AppException, Unit> =
        either {
            credentialsRepository.removeByGroupUid(groupUid)
        }

    fun removeGroups(groupUids: List<String>): Either<AppException, Unit> =
        either {
            for (uid in groupUids) {
                credentialsRepository.removeByGroupUid(uid)
            }
        }

    fun getGroupCredentialsFlow(): Flow<List<GroupCredentials>> = credentialsRepository.getAllFlow()

    fun createExportToCsvUrl(credentials: GroupCredentials): String =
        exportUrlUseCase.createUrl(credentials)

    fun createShareUrl(credentials: GroupCredentials): String =
        groupUrlUseCase.createUrl(credentials)
}