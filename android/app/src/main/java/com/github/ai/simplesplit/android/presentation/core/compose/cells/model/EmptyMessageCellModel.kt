package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class EmptyMessageCellModel(
    override val id: String,
    val message: String
) : CellModel