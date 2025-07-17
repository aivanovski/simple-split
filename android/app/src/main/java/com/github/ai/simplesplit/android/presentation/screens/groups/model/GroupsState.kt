package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

sealed interface GroupsState {

    data object Loading : GroupsState

    data object Empty : GroupsState

    data class Error(
        val message: String
    ) : GroupsState

    data class Data(
        val cellViewModels: List<CellViewModel>
    ) : GroupsState
}