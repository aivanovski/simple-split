package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog

import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ButtonCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ButtonCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.TextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ButtonCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogIntent
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class ConfirmationDialogViewModel(
    private val themeProvider: ThemeProvider,
    private val router: Router,
    private val args: ConfirmationDialogArgs
) : CellsMviViewModel<ConfirmationDialogState, ConfirmationDialogIntent>(
    initialState = ConfirmationDialogState(),
    initialIntent = ConfirmationDialogIntent.Initialize
) {

    override fun handleIntent(intent: ConfirmationDialogIntent): Flow<ConfirmationDialogState> {
        return when (intent) {
            ConfirmationDialogIntent.Initialize -> flowOf(buildState())
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is ButtonCellEvent.OnClick -> {
                router.setResult(Dialog.ConfirmationDialog::class, true)
                router.exit()
            }
        }
    }

    private fun buildState(): ConfirmationDialogState {
        return ConfirmationDialogState(createCells(cellEventProvider))
    }

    private fun createCells(eventProvider: CellEventProvider): List<CellViewModel> {
        val theme = themeProvider.theme

        return listOf(
            TextCellViewModel(
                TextCellModel(
                    id = "message",
                    text = args.message,
                    textSize = TextSize.TITLE_MEDIUM,
                    textColor = theme.colors.primaryText
                )
            ),
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space",
                    height = ElementMargin
                )
            ),
            ButtonCellViewModel(
                ButtonCellModel(
                    id = "confirm_button",
                    text = args.buttonTitle,
                    buttonColor = theme.colors.redButtonColor
                ),
                eventProvider
            )
        )
    }
}