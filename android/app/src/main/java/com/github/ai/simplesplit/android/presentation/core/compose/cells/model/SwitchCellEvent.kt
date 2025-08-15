package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent

interface SwitchCellEvent : CellEvent {

    data class OnCheckChanged(
        val cellId: String,
        val isChecked: Boolean
    ) : SwitchCellEvent
}