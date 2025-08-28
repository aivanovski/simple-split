package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme

@Composable
fun EmptyState(
    text: String,
    textAlign: TextAlign = TextAlign.Center,
    modifier: Modifier = Modifier
) {
    Text(
        text = text,
        style = TextSize.TITLE_LARGE.toTextStyle(),
        color = TextColor.PRIMARY.toColor(),
        textAlign = textAlign,
        modifier = modifier
    )
}

@Preview
@Composable
fun EmptyTextPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        CenteredBox {
            EmptyState("No data")
        }
    }
}