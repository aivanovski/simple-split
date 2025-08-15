package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent

sealed interface DropDownCellEvent : CellEvent {
    data class OnOptionSelect(
        val cellId: String,
        val selectedOption: String
    ) : DropDownCellEvent
}