package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent

sealed interface ExpenseCellEvent : CellEvent {
    data class OnClick(val cellId: String) : ExpenseCellEvent
    data class OnLongClick(val cellId: String) : ExpenseCellEvent
}