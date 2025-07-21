package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.unit.Dp
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class ShapedSpaceCellModel(
    override val id: String,
    val height: Dp,
    val shape: CornersShape
) : CellModel