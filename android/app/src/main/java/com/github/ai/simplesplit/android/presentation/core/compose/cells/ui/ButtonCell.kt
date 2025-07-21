package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ButtonCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ButtonCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ButtonCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin

@Composable
fun ButtonCell(viewModel: ButtonCellViewModel) {
    val model = viewModel.model

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(ButtonCellEvent.OnClick(model.id))
    }

    Button(
        onClick = onClick,
        modifier = Modifier
            .padding(
                start = ElementMargin,
                end = ElementMargin,
                bottom = SmallMargin
            )
            .fillMaxWidth(),
        colors = ButtonDefaults.buttonColors(
            containerColor = model.buttonColor
        )
    ) {
        Text(
            text = model.text
        )
    }
}

@Preview
@Composable
fun ButtonCellPreview() {
    ThemedPreview(theme = LightTheme) {
        ButtonCell(newButtonCell(text = "Remove group"))
    }
}

@Composable
fun newButtonCell(
    text: String = "Text",
    color: Color = AppTheme.theme.colors.redButtonColor
) = ButtonCellViewModel(
    model = ButtonCellModel(
        id = "id",
        text = text,
        buttonColor = color
    ),
    eventProvider = PreviewEventProvider
)