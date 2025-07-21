package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel

import androidx.compose.runtime.Stable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellModel

@Stable
class ExpenseCellViewModel(
    override val model: ExpenseCellModel,
    val eventProvider: CellEventProvider
) : CellViewModel {

    fun sendEvent(event: ExpenseCellEvent) {
        eventProvider.sendEvent(event)
    }
}