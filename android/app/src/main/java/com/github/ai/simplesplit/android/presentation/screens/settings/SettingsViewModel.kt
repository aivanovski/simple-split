package com.github.ai.simplesplit.android.presentation.screens.settings

import com.github.ai.simplesplit.android.data.settings.Settings
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.screens.settings.cells.SettingsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.settings.model.SettingsIntent
import com.github.ai.simplesplit.android.presentation.screens.settings.model.SettingsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class SettingsViewModel(
    private val interactor: SettingsInteractor,
    private val settings: Settings,
    private val cellFactory: SettingsCellFactory,
    private val router: Router
) : CellsMviViewModel<SettingsState, SettingsIntent>(
    initialState = SettingsState.Loading,
    initialIntent = SettingsIntent.Initialize
) {

    override fun handleIntent(intent: SettingsIntent): Flow<SettingsState> {
        return when (intent) {
            SettingsIntent.Initialize -> loadData()
            SettingsIntent.OnBackClick -> nonStateAction { navigateBack() }
        }
    }

    private fun loadData(): Flow<SettingsState> =
        flowOf(
            SettingsState.Data(
                cellViewModels = cellFactory.createCells(
                    settings = settings,
                    eventProvider = cellEventProvider
                )
            )
        )

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is SwitchCellEvent.OnCheckChanged -> {
                settings.isSslVerificationEnabled = event.isChecked
                interactor.onSslVerificationEnabledChanged()
            }

            is DropDownCellEvent.OnOptionSelect -> {
                settings.serverUrl = event.selectedOption
                interactor.onServerUrlChanged()
            }
        }
    }

    private fun navigateBack() {
        router.exit()
    }
}