package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class GroupDetailsIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : GroupDetailsIntent()
    data object ReloadData : GroupDetailsIntent()
    data object OnBackClick : GroupDetailsIntent()
    data object OnFabClick : GroupDetailsIntent()
}