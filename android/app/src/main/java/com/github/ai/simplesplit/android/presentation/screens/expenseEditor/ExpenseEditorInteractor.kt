package com.github.ai.simplesplit.android.presentation.screens.expenseEditor

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.data.repository.ExpenseRepository
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.api.PostExpenseRequest
import com.github.ai.split.api.PutExpenseRequest
import com.github.ai.split.api.UserUidDto

class ExpenseEditorInteractor(
    private val expenseRepository: ExpenseRepository,
    private val credentialsRepository: GroupCredentialsRepository
) {

    suspend fun updateExpense(
        credentials: GroupCredentials,
        expenseUid: String,
        title: String?,
        amount: Double?,
        payerUid: String?
    ): Either<AppException, ExpenseDto> =
        either {
            val request = PutExpenseRequest(
                title = title,
                description = null,
                amount = amount,
                paidBy = if (payerUid != null) listOf(UserUidDto(payerUid)) else emptyList(),
                isSplitBetweenAll = true,
                splitBetween = null
            )

            expenseRepository.updateExpense(
                password = credentials.password,
                expenseUid = expenseUid,
                request = request
            ).bind().expense
        }

    suspend fun createExpense(
        groupUid: String,
        title: String,
        amount: Double,
        payerUid: String
    ): Either<AppException, ExpenseDto> =
        either {
            val credentials = credentialsRepository.getByGroupUid(groupUid)
                ?: raise(AppException(message = "Failed to load credentials for group"))

            val request = PostExpenseRequest(
                groupUid = groupUid,
                title = title,
                description = "",
                amount = amount,
                paidBy = listOf(UserUidDto(payerUid)),
                isSplitBetweenAll = true,
                splitBetween = emptyList()
            )

            expenseRepository.createExpense(
                password = credentials.password,
                request = request
            ).bind().expense
        }
}