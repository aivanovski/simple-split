package com.github.ai.simplesplit.android.presentation.screens.expenseEditor

import arrow.core.Either
import arrow.core.raise.either
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.data.repository.ExpenseRepository
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.ExpenseDto
import com.github.ai.split.api.UserUidDto
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.request.PutExpenseRequest

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
                paidBy = if (payerUid != null) listOf(UserUidDto(uid = payerUid)) else null,
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
                amount = amount,
                description = "",
                paidBy = listOf(UserUidDto(uid = payerUid)),
                isSplitBetweenAll = true,
                splitBetween = null
            )

            expenseRepository.createExpense(
                password = credentials.password,
                request = request
            ).bind().expense
        }
}