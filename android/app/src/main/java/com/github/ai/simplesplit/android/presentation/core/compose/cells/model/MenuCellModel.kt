package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.Dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

data class MenuCellModel(
    override val id: String,
    val icon: ImageVector?,
    val title: String,
    val height: Dp
) : CellModel