package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel

@Immutable
data class ExpenseCellModel(
    override val id: String,
    val title: String,
    val description: String,
    val members: List<String>,
    val amount: String,
    val date: String,
    val shape: CornersShape
) : CellModel