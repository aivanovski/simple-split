package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

@Immutable
data class ExpenseDetailsDialogState(
    val cellViewModels: List<CellViewModel> = emptyList()
)