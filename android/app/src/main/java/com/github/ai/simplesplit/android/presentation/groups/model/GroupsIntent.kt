package com.github.ai.simplesplit.android.presentation.groups.model

sealed interface GroupsIntent {
    data object Initialize : GroupsIntent
    // data class OnGroupClick(val groupId: String) : GroupsIntent
}