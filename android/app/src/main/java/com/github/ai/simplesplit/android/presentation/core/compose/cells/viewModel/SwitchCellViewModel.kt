package com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel

import androidx.compose.runtime.Stable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.MutableCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellModel

@Stable
class SwitchCellViewModel(
    model: SwitchCellModel,
    private val eventProvider: CellEventProvider
) : MutableCellViewModel<SwitchCellModel>(model) {

    fun sendIntent(event: SwitchCellEvent) {
        handleEvent(event)
        eventProvider.sendEvent(event)
    }

    private fun handleEvent(event: SwitchCellEvent) {
        when (event) {
            is SwitchCellEvent.OnCheckChanged -> {
                observableModel.value = observableModel.value.copy(
                    isChecked = event.isChecked
                )
            }
        }
    }
}