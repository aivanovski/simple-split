package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class GroupsIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : GroupsIntent()
    data object ReloadData : GroupsIntent()
    data object ReloadDataInBackground : GroupsIntent()
    data object OnAddButtonClick : GroupsIntent()
    data object OnCreateGroupClick : GroupsIntent()
    data object OnAddGroupByUrlClick : GroupsIntent()
    data class OnGroupClick(val groupUid: String) : GroupsIntent()
    data class OnGroupLongClick(val groupUid: String) : GroupsIntent()
    data class OnEditGroupClick(val groupUid: String) : GroupsIntent()
    data class OnRemoveGroupClick(val groupUid: String) : GroupsIntent()
    data class OnRemoveGroupConfirmed(val groupUid: String) : GroupsIntent()
    data class OpenUrl(val url: String) : GroupsIntent()
    data class ShareUrl(val url: String) : GroupsIntent()
}