package com.github.ai.simplesplit.android.presentation.groups.model

import com.github.ai.simplesplit.android.domain.model.Group

sealed interface GroupsState {

    data object Loading : GroupsState

    data class Data(
        val groups: List<Group>
    ) : GroupsState
}