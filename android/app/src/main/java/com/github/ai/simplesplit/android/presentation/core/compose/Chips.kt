package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin

@Composable
fun TextSquaredChip(
    text: String,
    textSize: TextSize = TextSize.BODY_MEDIUM,
    textColor: Color = AppTheme.theme.colors.primaryText,
    cardColor: Color = AppTheme.theme.colors.chipColor
) {
    Box(
        contentAlignment = Alignment.Center,
        modifier = Modifier
            .size(24.dp)
            .background(
                color = cardColor,
                shape = CircleShape
            )
    ) {
        Text(
            text = text,
            style = textSize.toTextStyle(),
            color = textColor
        )
    }
}

@Composable
@Preview
fun ChipPreview() {
    ThemedPreview(
        theme = LightTheme,
        background = LightTheme.colors.cardPrimaryBackground
    ) {
        Column(
            modifier = Modifier
                .padding(all = ElementMargin)
        ) {
            TextSquaredChip(
                text = "M"
            )

            Spacer(Modifier.height(SmallMargin))

            TextSquaredChip(
                text = "B",
                textSize = TextSize.TITLE_MEDIUM
            )
        }
    }
}