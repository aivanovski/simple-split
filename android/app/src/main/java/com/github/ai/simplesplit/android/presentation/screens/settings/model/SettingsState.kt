package com.github.ai.simplesplit.android.presentation.screens.settings.model

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel

sealed class SettingsState {
    data object Loading : SettingsState()
    data class Error(val message: String) : SettingsState()
    data class Data(val cellViewModels: List<CellViewModel>) : SettingsState()
}