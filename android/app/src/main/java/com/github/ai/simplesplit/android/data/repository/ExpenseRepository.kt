package com.github.ai.simplesplit.android.data.repository

import arrow.core.Either
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.split.api.DeleteExpenseResponse
import com.github.ai.split.api.PostExpenseRequest
import com.github.ai.split.api.PostExpenseResponse
import com.github.ai.split.api.PutExpenseRequest
import com.github.ai.split.api.PutExpenseResponse

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

    suspend fun updateExpense(
        password: String,
        expenseUid: String,
        request: PutExpenseRequest
    ): Either<AppException, PutExpenseResponse> =
        api.putExpense(
            password = password,
            expenseUid = expenseUid,
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