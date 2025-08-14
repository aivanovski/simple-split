package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogIntent
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogState
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuItem
import com.github.ai.simplesplit.android.utils.CellId
import com.github.ai.simplesplit.android.utils.CellIdPayload.IntPayload
import com.github.ai.simplesplit.android.utils.format
import com.github.ai.simplesplit.android.utils.parseCellId
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class MenuDialogViewModel(
    private val router: Router,
    private val args: MenuDialogArgs
) : CellsMviViewModel<MenuDialogState, MenuDialogIntent>(
    initialState = MenuDialogState(),
    initialIntent = MenuDialogIntent.Initialize
) {

    override fun handleIntent(intent: MenuDialogIntent): Flow<MenuDialogState> {
        return when (intent) {
            MenuDialogIntent.Initialize -> flowOf(buildState())
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is MenuCellEvent.OnClick -> onMenuItemClicked(event.cellId)
        }
    }

    private fun buildState(): MenuDialogState {
        return MenuDialogState(createCells(args.items, cellEventProvider))
    }

    private fun onMenuItemClicked(cellId: String) {
        val actionId = getActionIdFromCellId(cellId) ?: return

        val item = args.items
            .firstOrNull { item -> item.actionId == actionId }
            ?: return

        router.setResult(Dialog.MenuDialog::class, item)
        router.exit()
    }

    private fun createCells(
        items: List<MenuItem>,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        return items.mapIndexed { index, item ->
            val model = MenuCellModel(
                id = CellId("menu_item", IntPayload(item.actionId)).format(),
                icon = item.icon.vector,
                title = item.text
            )

            MenuCellViewModel(model, eventProvider)
        }
    }

    private fun getActionIdFromCellId(cellId: String): Int? {
        return cellId.parseCellId()
            ?.payload
            ?.let { payload -> payload as? IntPayload }
            ?.intValue
    }
}