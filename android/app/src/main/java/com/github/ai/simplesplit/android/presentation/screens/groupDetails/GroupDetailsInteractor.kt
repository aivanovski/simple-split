package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import arrow.core.Either
import com.github.ai.simplesplit.android.data.repository.ExpenseRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.GroupDto

class GroupDetailsInteractor(
    private val groupRepository: GroupRepository,
    private val expenseRepository: ExpenseRepository
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
}