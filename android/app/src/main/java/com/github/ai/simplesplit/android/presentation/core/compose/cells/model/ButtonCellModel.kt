package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class ButtonCellModel(
    override val id: String,
    val text: String,
    val buttonColor: Color
) : CellModel