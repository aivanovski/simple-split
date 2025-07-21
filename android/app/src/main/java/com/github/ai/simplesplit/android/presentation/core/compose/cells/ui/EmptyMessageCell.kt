package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.EmptyMessageCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.EmptyMessageCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DoubleElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun EmptyMessageCell(viewModel: EmptyMessageCellViewModel) {
    val model = viewModel.model

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .height(height = model.height)
            .padding(horizontal = DoubleElementMargin),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = model.message,
            textAlign = TextAlign.Center,
            style = TextSize.BODY_MEDIUM.toTextStyle(),
            color = AppTheme.theme.colors.secondaryText
        )
    }
}

@Composable
@Preview
fun EmptyMessageCellPreview() {
    ThemedPreview(theme = LightTheme) {
        EmptyMessageCell(newEmptyMessageCell())
    }
}

@Composable
fun newEmptyMessageCell(message: String = "No items found") =
    EmptyMessageCellViewModel(
        model = EmptyMessageCellModel(
            id = "empty_message_id",
            message = message,
            height = 200.dp
        )
    )