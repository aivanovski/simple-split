package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ButtonCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.ButtonCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.TextCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newTextCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ButtonCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DialogCardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogState

@Composable
fun ConfirmationDialogScreen(viewModel: ConfirmationDialogViewModel) {
    val state by viewModel.state.collectAsState()

    ConfirmationDialogScreen(
        state = state
    )
}

@Composable
private fun ConfirmationDialogScreen(state: ConfirmationDialogState) {
    Box(
        modifier = Modifier
    ) {
        Card(
            shape = RoundedCornerShape(DialogCardCornerSize, DialogCardCornerSize, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.theme.colors.secondaryBackground
            )
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        top = ElementMargin,
                        start = ElementMargin,
                        end = ElementMargin,
                        bottom = ElementMargin
                    )
            ) {
                items(state.cellViewModels) { cellViewModel ->
                    RenderCell(cellViewModel)
                }
            }
        }
    }
}

@Composable
private fun RenderCell(cellViewModel: CellViewModel) {
    when (cellViewModel) {
        is TextCellViewModel -> TextCell(cellViewModel)
        is SpaceCellViewModel -> SpaceCell(cellViewModel)
        is ButtonCellViewModel -> ButtonCell(cellViewModel)
        else -> throw IllegalArgumentException(
            "Unknown cell type: ${cellViewModel::class.simpleName}"
        )
    }
}

@Preview
@Composable
fun ConfirmationDialogScreenPreview() {
    ThemedPreview(
        theme = LightTheme,
        background = Color.Transparent
    ) {
        ConfirmationDialogScreen(
            state = newConfirmationDialogState()
        )
    }
}

@Composable
private fun newConfirmationDialogState() =
    ConfirmationDialogState(
        cellViewModels = listOf(
            newTextCell(
                text = stringResource(R.string.remove_group_confirmation_message),
                textSize = TextSize.TITLE_LARGE
            ),
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space",
                    height = ElementMargin
                )
            ),
            ButtonCellViewModel(
                ButtonCellModel(
                    id = "confirm",
                    text = "Remove",
                    buttonColor = AppTheme.theme.colors.redButtonColor
                ),
                eventProvider = PreviewEventProvider
            )
        )
    )