package com.github.ai.simplesplit.android.presentation.core.compose.cells.model

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class BottomSheetHeaderCellModel(
    override val id: String,
    val title: String,
    val description: String,
    val titleTextSize: TextSize,
    val descriptionTextSize: TextSize,
    val icon: ImageVector
) : CellModel