package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent

sealed interface BottomSheetHeaderCellEvent : CellEvent {
    data class OnIconClick(val cellId: String) : BottomSheetHeaderCellEvent
}