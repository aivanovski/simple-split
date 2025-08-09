package com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.BottomSheetHeaderCellModel

@Immutable
class BottomSheetHeaderCellViewModel(
    override val model: BottomSheetHeaderCellModel,
    private val eventProvider: CellEventProvider
) : CellViewModel {

    fun sendEvent(event: CellEvent) {
        eventProvider.sendEvent(event)
    }
}