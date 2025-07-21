package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog

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
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.MenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newMenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DialogCardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogState

@Composable
fun MenuDialogScreen(viewModel: MenuDialogViewModel) {
    val state by viewModel.state.collectAsState()

    MenuDialogScreen(
        state = state
    )
}

@Composable
private fun MenuDialogScreen(state: MenuDialogState) {
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
                        top = ElementMargin
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
        is MenuCellViewModel -> MenuCell(cellViewModel)
        else -> throw IllegalArgumentException()
    }
}

@Preview
@Composable
fun MenuDialogScreenPreview() {
    ThemedPreview(
        theme = LightTheme,
        background = Color.Transparent
    ) {
        MenuDialogScreen(
            state = newMenuDialogState()
        )
    }
}

private fun newMenuDialogState() =
    MenuDialogState(
        cellViewModels = listOf(
            newMenuCell(),
            newMenuCell()
        )
    )