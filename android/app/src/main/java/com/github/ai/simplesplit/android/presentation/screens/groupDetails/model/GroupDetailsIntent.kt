package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class GroupDetailsIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : GroupDetailsIntent()
    data object ReloadData : GroupDetailsIntent()
    data object ReloadDataInBackground : GroupDetailsIntent()
    data object OnBackClick : GroupDetailsIntent()
    data object OnFabClick : GroupDetailsIntent()
    data object OnMenuClick : GroupDetailsIntent()
    data object OnEditGroupClick : GroupDetailsIntent()
    data object OnRemoveGroupClick : GroupDetailsIntent()
    data object OnRemoveGroupConfirmed : GroupDetailsIntent()
    data class OnExpenseClick(val expenseUid: String) : GroupDetailsIntent()
    data class OnExpenseLongClick(val expenseUid: String) : GroupDetailsIntent()
    data class OnEditExpenseClick(val expenseUid: String) : GroupDetailsIntent()
    data class OnRemoveExpenseClick(val expenseUid: String) : GroupDetailsIntent()
    data class OnRemoveExpenseConfirmed(val expenseUid: String) : GroupDetailsIntent()
    data class OpenUrl(val url: String) : GroupDetailsIntent()
    data class ShareGroupUrl(val url: String) : GroupDetailsIntent()
}