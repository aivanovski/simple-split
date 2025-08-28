package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model

import kotlinx.serialization.Serializable

@Serializable
data class SelectCurrencyDialogArgs(
    val selectedIsoCurrencyCode: String? = null
)