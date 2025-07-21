package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent

sealed interface MenuCellEvent : CellEvent {
    data class OnClick(val cellId: String) : MenuCellEvent
}