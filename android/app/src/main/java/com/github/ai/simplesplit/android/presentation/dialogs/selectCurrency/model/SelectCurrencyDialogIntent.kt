package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class SelectCurrencyDialogIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : SelectCurrencyDialogIntent()
    data object Dismiss : SelectCurrencyDialogIntent()
    data object OnResetQueryClick : SelectCurrencyDialogIntent()
    data class OnQueryTextChange(val query: String) : SelectCurrencyDialogIntent(isImmediate = true)
    data class OnCurrencySelected(val currencyIsoCode: String) : SelectCurrencyDialogIntent()
    data class FilterCurrencies(val query: String) : SelectCurrencyDialogIntent()
}