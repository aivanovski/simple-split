package com.github.ai.simplesplit.android.presentation.screens.groups.model

import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

sealed interface GroupsState {

    data object Loading : GroupsState

    data class Empty(
        val error: ErrorMessage? = null
    ) : GroupsState

    data class Error(
        val message: ErrorMessage
    ) : GroupsState

    data class Data(
        val cellViewModels: List<CellViewModel> = emptyList(),
        val error: ErrorMessage? = null
    ) : GroupsState
}