package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.BottomSheetHeaderCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.ExpenseDetailsDialogCellFactory.CellId
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsAction
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogIntent
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ExpenseDetailsDialogViewModel(
    private val cellFactory: ExpenseDetailsDialogCellFactory,
    private val router: Router,
    private val args: ExpenseDetailsDialogArgs
) : CellsMviViewModel<ExpenseDetailsDialogState, ExpenseDetailsDialogIntent>(
    initialState = ExpenseDetailsDialogState(),
    initialIntent = ExpenseDetailsDialogIntent.Initialize
) {

    override fun handleIntent(intent: ExpenseDetailsDialogIntent): Flow<ExpenseDetailsDialogState> {
        return when (intent) {
            ExpenseDetailsDialogIntent.Initialize -> flowOf(buildState())
            ExpenseDetailsDialogIntent.Dismiss -> nonStateAction { router.exit() }

            ExpenseDetailsDialogIntent.OnEditMenuClick ->
                nonStateAction {
                    router.setResult(Dialog.ExpenseDetails::class, ExpenseDetailsAction.EDIT)
                    router.exit()
                }

            ExpenseDetailsDialogIntent.OnRemoveMenuClick ->
                nonStateAction {
                    router.setResult(Dialog.ExpenseDetails::class, ExpenseDetailsAction.REMOVE)
                    router.exit()
                }
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is BottomSheetHeaderCellEvent.OnIconClick ->
                sendIntent(ExpenseDetailsDialogIntent.Dismiss)

            is MenuCellEvent.OnClick -> {
                val id = CellId.valueOf(event.cellId)
                when (id) {
                    CellId.EDIT_MENU -> sendIntent(ExpenseDetailsDialogIntent.OnEditMenuClick)
                    CellId.REMOVE_MENU -> sendIntent(ExpenseDetailsDialogIntent.OnRemoveMenuClick)
                }
            }
        }
    }

    private fun buildState(): ExpenseDetailsDialogState {
        return ExpenseDetailsDialogState(
            cellViewModels = cellFactory.createCells(
                expense = args.expense,
                eventProvider = cellEventProvider
            )
        )
    }
}