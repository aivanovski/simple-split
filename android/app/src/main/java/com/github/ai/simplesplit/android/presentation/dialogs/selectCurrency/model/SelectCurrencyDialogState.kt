package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.utils.StringUtils

@Immutable
sealed interface SelectCurrencyDialogState {

    @Immutable
    data class Data(
        val isLoading: Boolean = false,
        val query: String = StringUtils.EMPTY,
        val cellViewModels: List<CellViewModel> = emptyList()
    ) : SelectCurrencyDialogState

    @Immutable
    data class Error(
        val message: ErrorMessage
    ) : SelectCurrencyDialogState
}