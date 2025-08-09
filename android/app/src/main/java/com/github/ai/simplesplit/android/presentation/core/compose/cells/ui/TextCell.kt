package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.TextColor
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.TextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.preview.longText
import com.github.ai.simplesplit.android.presentation.core.compose.preview.shortText
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toColor
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun TextCell(viewModel: TextCellViewModel) {
    val model = viewModel.model

    Text(
        text = model.text,
        color = model.textColor.toColor(),
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
                newTextCell(
                    textSize = TextSize.TITLE_LARGE
                )
            )
            TextCell(
                newTextCell()
            )
            ElementSpace()
            TextCell(
                newTextCell(
                    text = longText()
                )
            )
            ElementSpace()
            TextCell(
                newTextCell(
                    text = "Error message",
                    textColor = TextColor.ERROR
                )
            )
        }
    }
}

@Composable
fun newTextCell(
    text: String = shortText(),
    textSize: TextSize = TextSize.BODY_LARGE,
    textColor: TextColor = TextColor.PRIMARY
) = TextCellViewModel(
    model = TextCellModel(
        id = "id",
        text = text,
        textSize = textSize,
        textColor = textColor
    )
)