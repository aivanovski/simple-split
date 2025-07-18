package com.github.ai.simplesplit.android.presentation.screens.expenseEditor

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.model.exception.AppException

class ExpenseEditorInteractor(
    private val repository: GroupRepository,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun createExpense(
        groupUid: String,
        title: String,
        amount: Double,
        payerName: String
    ): Either<AppException, Unit> =
        either {
            val credentials = credentialsRepository.getByGroupUid(groupUid)
                ?: raise(AppException(message = "Fail to load credentials for group"))


        }
}