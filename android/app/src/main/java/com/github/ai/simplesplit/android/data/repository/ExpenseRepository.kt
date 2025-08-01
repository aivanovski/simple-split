package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.request.PostExpenseRequest
import com.github.ai.split.api.response.DeleteExpenseResponse
import com.github.ai.split.api.response.PostExpenseResponse

class ExpenseRepository(
    private val api: ApiClient
) {

    suspend fun createExpense(
        password: String,
        request: PostExpenseRequest
    ): Either<AppException, PostExpenseResponse> =
        api.postExpense(
            password = password,
            request = request
        )

    suspend fun removeExpense(
        password: String,
        expenseUid: String
    ): Either<AppException, DeleteExpenseResponse> =
        api.removeExpense(
            password = password,
            expenseUid = expenseUid
        )
}