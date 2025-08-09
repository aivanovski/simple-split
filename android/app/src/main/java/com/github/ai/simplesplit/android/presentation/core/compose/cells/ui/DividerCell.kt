package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Divider
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DividerCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme

@Composable
fun DividerCell(viewModel: DividerCellViewModel) {
    val model = viewModel.model

    Divider(
        color = AppTheme.theme.colors.dividerOnPrimary,
        modifier = Modifier.padding(horizontal = model.padding)
    )
}

@Composable
@Preview
fun DividerCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            ElementSpace()
            DividerCell(newDividerCell())
            ElementSpace()
        }
    }
}

@Composable
fun newDividerCell(padding: Dp = 0.dp) =
    DividerCellViewModel(
        model = DividerCellModel(
            id = "divider_id",
            padding = padding
        )
    )