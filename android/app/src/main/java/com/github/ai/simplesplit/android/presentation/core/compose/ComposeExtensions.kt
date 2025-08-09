package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.TextStyle
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.CardCornerSize

@Composable
inline fun <T> rememberCallback(crossinline block: (T) -> Unit): (T) -> Unit {
    return remember { { value -> block.invoke(value) } }
}

@Composable
inline fun rememberOnClickedCallback(crossinline block: () -> Unit): () -> Unit {
    return remember { { block.invoke() } }
}

fun CornersShape.toComposeShape(): RoundedCornerShape =
    when (this) {
        CornersShape.TOP -> RoundedCornerShape(
            topStart = CardCornerSize,
            topEnd = CardCornerSize
        )

        CornersShape.BOTTOM -> RoundedCornerShape(
            bottomStart = CardCornerSize,
            bottomEnd = CardCornerSize
        )

        CornersShape.ALL -> RoundedCornerShape(
            size = CardCornerSize
        )

        CornersShape.NONE -> RoundedCornerShape(size = 0.dp)
    }

@Composable
fun TextSize.toTextStyle(): TextStyle =
    when (this) {
        TextSize.TITLE_LARGE -> AppTheme.theme.typography.titleLarge
        TextSize.TITLE_MEDIUM -> AppTheme.theme.typography.titleMedium
        TextSize.BODY_LARGE -> AppTheme.theme.typography.bodyLarge
        TextSize.BODY_MEDIUM -> AppTheme.theme.typography.bodyMedium
        TextSize.BODY_SMALL -> AppTheme.theme.typography.bodySmall
    }

@Composable
fun TextColor.toColor(): Color =
    when (this) {
        TextColor.PRIMARY -> AppTheme.theme.colors.primaryText
        TextColor.SECONDARY -> AppTheme.theme.colors.secondaryText
        TextColor.ERROR -> AppTheme.theme.colors.errorText
    }