package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.ExpenseRepository
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.domain.usecase.CreateExportUrlUseCase
import com.github.ai.simplesplit.android.domain.usecase.CreateGroupUrlUseCase
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto

class GroupDetailsInteractor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository,
    private val credentialsRepository: GroupCredentialsRepository,
    private val exportUrlUseCase: CreateExportUrlUseCase,
    private val groupUrlUseCase: CreateGroupUrlUseCase
) {

    suspend fun getGroup(
        groupUid: String,
        password: String
    ): Either<AppException, GroupDto> =
        groupRepository.getGroups(
            uids = listOf(groupUid),
            passwords = listOf(password)
        )
            // TODO: handle error if group not found
            .map { groups -> groups.first() }

    suspend fun removeExpense(
        password: String,
        expenseUid: String
    ): Either<AppException, GroupDto> =
        expenseRepository.removeExpense(
            password = password,
            expenseUid = expenseUid
        ).map { it.group }

    fun removeGroup(groupUid: String): Either<AppException, Unit> =
        either {
            credentialsRepository.removeByGroupUid(groupUid)
        }

    fun createExportToCsvUrl(credentials: GroupCredentials): String =
        exportUrlUseCase.createUrl(credentials)

    fun createShareUrl(credentials: GroupCredentials): String =
        groupUrlUseCase.createUrl(credentials)
}