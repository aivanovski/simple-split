package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.HeaderCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.HeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DoubleElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun HeaderCell(viewModel: HeaderCellViewModel) {
    val model = viewModel.model

    Text(
        text = model.text,
        style = model.textSize.toTextStyle(),
        color = model.textColor,
        modifier = Modifier.padding(
            horizontal = DoubleElementMargin
        )
    )
}

@Composable
@Preview
fun HeaderCellPreview() {
    ThemedPreview(theme = LightTheme) {
        HeaderCell(newHeaderCell())
    }
}

@Composable
fun newHeaderCell(text: String = "Header text") =
    HeaderCellViewModel(
        model = HeaderCellModel(
            id = "id",
            text = text,
            textSize = TextSize.TITLE_MEDIUM,
            textColor = AppTheme.theme.colors.primaryText
        )
    )