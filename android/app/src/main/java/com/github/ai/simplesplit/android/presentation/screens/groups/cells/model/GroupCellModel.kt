package com.github.ai.simplesplit.android.presentation.screens.groups.cells.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class GroupCellModel(
    override val id: String,
    val title: String,
    val description: String,
    val members: String,
    val amount: String
) : CellModel

sealed interface GroupCellEvent : CellEvent {
    data class OnClick(val cellId: String) : GroupCellEvent
    data class OnLongClick(val cellId: String) : GroupCellEvent
}