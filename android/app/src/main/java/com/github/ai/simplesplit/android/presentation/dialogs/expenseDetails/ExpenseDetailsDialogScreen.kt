package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.TextColor
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.BottomSheetHeaderCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.DividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.MenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.TextCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newBottomSheetHeaderCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newDividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newMenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newSpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newTextCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.BottomSheetHeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DialogCardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.TinyMargin
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogState

@Composable
fun ExpenseDetailsDialogScreen(viewModel: ExpenseDetailsDialogViewModel) {
    val state by viewModel.state.collectAsState()

    ExpenseDetailsDialogScreen(
        state = state
    )
}

@Composable
private fun ExpenseDetailsDialogScreen(state: ExpenseDetailsDialogState) {
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
            ) {
                items(state.cellViewModels) { cellViewModel ->
                    when (cellViewModel) {
                        is TextCellViewModel -> TextCell(cellViewModel)
                        is SpaceCellViewModel -> SpaceCell(cellViewModel)
                        is DividerCellViewModel -> DividerCell(cellViewModel)
                        is BottomSheetHeaderCellViewModel -> BottomSheetHeaderCell(cellViewModel)
                        is MenuCellViewModel -> MenuCell(cellViewModel)
                        else -> throw IllegalArgumentException()
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun ExpenseDetailsScreenPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        ExpenseDetailsDialogScreen(
            state = ExpenseDetailsDialogState(
                cellViewModels = listOf(
                    newBottomSheetHeaderCell(
                        title = "Beer",
                        description = "15.42$",
                        titleTextSize = TextSize.TITLE_MEDIUM,
                        descriptionTextSize = TextSize.TITLE_LARGE
                    ),
                    newSpaceCell(
                        height = TinyMargin
                    ),
                    newTextCell(
                        text = "added on Jan 01",
                        textSize = TextSize.BODY_MEDIUM,
                        textColor = TextColor.SECONDARY
                    ),
                    newSpaceCell(
                        height = HalfMargin
                    ),
                    newDividerCell(),
                    newSpaceCell(
                        height = ElementMargin
                    ),
                    newTextCell(
                        text = "Mickey paid 15.42$"
                    ),
                    newSpaceCell(
                        height = ElementMargin
                    ),
                    newTextCell(
                        text = "Donald owe 3.00$",
                        textColor = TextColor.SECONDARY
                    ),
                    newTextCell(
                        text = "Goofy owe 3.00$",
                        textColor = TextColor.SECONDARY
                    ),
                    newSpaceCell(
                        height = GroupMargin
                    ),
                    newDividerCell(),
                    newSpaceCell(
                        height = HalfMargin
                    ),
                    newMenuCell(
                        icon = AppIcon.EDIT.vector,
                        title = "Edit"
                    ),
                    newMenuCell(
                        icon = AppIcon.REMOVE.vector,
                        title = "Remove"
                    )
                )
            )
        )
    }
}