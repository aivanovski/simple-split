package com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel

import androidx.compose.runtime.Stable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellModel

@Stable
class GroupCellViewModel(
    override val model: GroupCellModel,
    val eventProvider: CellEventProvider
) : CellViewModel {

    fun sendEvent(event: GroupCellEvent) {
        eventProvider.sendEvent(event)
    }
}