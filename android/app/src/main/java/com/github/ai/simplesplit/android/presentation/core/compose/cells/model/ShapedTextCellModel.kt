package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.Color
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class ShapedTextCellModel(
    override val id: String,
    val text: String,
    val textSize: TextSize,
    val textColor: Color,
    val shape: CornersShape
) : CellModel