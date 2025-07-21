package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

@Immutable
data class ConfirmationDialogState(
    val cellViewModels: List<CellViewModel> = emptyList()
)