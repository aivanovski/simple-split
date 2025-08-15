package com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel

import androidx.compose.runtime.Stable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.MutableCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellModel

@Stable
class DropDownCellViewModel(
    initialModel: DropDownCellModel,
    private val eventProvider: CellEventProvider
) : MutableCellViewModel<DropDownCellModel>(initialModel) {

    fun sendEvent(event: DropDownCellEvent) {
        handleEvent(event)
        eventProvider.sendEvent(event)
    }

    private fun handleEvent(event: DropDownCellEvent) {
        when (event) {
            is DropDownCellEvent.OnOptionSelect -> {
                observableModel.value = observableModel.value.copy(
                    selectedOption = event.selectedOption
                )
            }
        }
    }
}