package com.github.ai.simplesplit.android.presentation.screens.groupDetails.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

sealed interface GroupDetailsState {

    data object Loading : GroupDetailsState

    data class Error(
        val message: String
    ) : GroupDetailsState

    data class Data(
        val cellViewModels: List<CellViewModel>
    ) : GroupDetailsState
}