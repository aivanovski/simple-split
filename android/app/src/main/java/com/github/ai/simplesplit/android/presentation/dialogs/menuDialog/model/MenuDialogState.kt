package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

@Immutable
data class MenuDialogState(
    val cellViewModels: List<CellViewModel> = emptyList()
)