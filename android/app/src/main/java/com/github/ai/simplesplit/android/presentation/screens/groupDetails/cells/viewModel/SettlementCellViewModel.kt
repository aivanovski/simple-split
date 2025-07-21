package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel

import androidx.compose.runtime.Stable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellModel

@Stable
class SettlementCellViewModel(
    override val model: SettlementCellModel,
    val eventProvider: CellEventProvider
) : CellViewModel {

    fun sendEvent(event: SettlementCellEvent) {
        eventProvider.sendEvent(event)
    }
}