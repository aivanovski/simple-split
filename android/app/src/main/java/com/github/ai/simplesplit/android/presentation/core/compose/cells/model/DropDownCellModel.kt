package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class DropDownCellModel(
    override val id: String,
    val title: String,
    val options: List<String>,
    val selectedOption: String
) : CellModel