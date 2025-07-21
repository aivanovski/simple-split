package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.TextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.preview.longText
import com.github.ai.simplesplit.android.presentation.core.compose.preview.shortText
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun TextCell(viewModel: TextCellViewModel) {
    val model = viewModel.model

    Text(
        text = model.text,
        color = model.textColor,
        style = model.textSize.toTextStyle(),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ElementMargin
            )
    )
}

@Composable
@Preview
fun TextCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            ElementSpace()
            TextCell(
                newTextCellViewModel(
                    textSize = TextSize.TITLE_LARGE
                )
            )
            TextCell(
                newTextCellViewModel()
            )
            ElementSpace()
            TextCell(
                newTextCellViewModel(
                    text = longText()
                )
            )
            ElementSpace()
            TextCell(
                newTextCellViewModel(
                    text = "Error message",
                    textColor = AppTheme.theme.colors.errorText
                )
            )
        }
    }
}

@Composable
fun newTextCellViewModel(
    text: String = shortText(),
    textSize: TextSize = TextSize.BODY_LARGE,
    textColor: Color = AppTheme.theme.colors.primaryText
) = TextCellViewModel(
    model = TextCellModel(
        id = "id",
        text = text,
        textSize = textSize,
        textColor = textColor
    )
)