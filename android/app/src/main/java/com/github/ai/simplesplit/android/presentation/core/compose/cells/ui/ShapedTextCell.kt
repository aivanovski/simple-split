package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ShapedTextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedTextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.preview.longText
import com.github.ai.simplesplit.android.presentation.core.compose.preview.shortText
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toComposeShape
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun ShapedTextCell(viewModel: ShapedTextCellViewModel) {
    val model = viewModel.model

    Card(
        shape = model.shape.toComposeShape(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.cardPrimaryBackground
        ),
        modifier = Modifier
            .padding(
                horizontal = ElementMargin
            )
    ) {
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
}

@Composable
@Preview
fun TitleCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            ElementSpace()
            ShapedTextCell(
                newTextCell(
                    textSize = TextSize.TITLE_LARGE,
                    shape = CornersShape.TOP
                )
            )
            ShapedTextCell(
                newTextCell(
                    shape = CornersShape.BOTTOM
                )
            )
            ElementSpace()
            ShapedTextCell(
                newTextCell(
                    text = longText()
                )
            )
            ElementSpace()
            ShapedTextCell(
                newTextCell(
                    text = "Error message"
                )
            )
        }
    }
}

@Composable
fun newTextCell(
    text: String = shortText(),
    textSize: TextSize = TextSize.BODY_LARGE,
    textColor: Color = AppTheme.theme.colors.primaryText,
    shape: CornersShape = CornersShape.ALL
) = ShapedTextCellViewModel(
    model = ShapedTextCellModel(
        id = "id",
        text = text,
        textSize = textSize,
        textColor = textColor,
        shape = shape
    )
)