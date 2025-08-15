package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class SwitchCellModel(
    override val id: String,
    val title: String,
    val description: String,
    val isChecked: Boolean,
    val isEnabled: Boolean
) : CellModel