package com.github.ai.simplesplit.android.presentation.core.compose.preview

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.Theme

@Composable
fun ThemedPreview(
    theme: Theme,
    background: Color = theme.colors.background,
    content: @Composable () -> Unit
) {
    AppTheme(theme = theme) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .background(
                    color = background
                )
        ) {
            content.invoke()
        }
    }
}

@Composable
fun ThemedScreenPreview(
    theme: Theme,
    background: Color = theme.colors.background,
    content: @Composable () -> Unit
) {
    AppTheme(theme = theme) {
        Box(
            modifier = Modifier
                .fillMaxSize()
                .background(
                    color = background
                )
        ) {
            content.invoke()
        }
    }
}

@Composable
fun ElementSpace() {
    Spacer(modifier = Modifier.height(ElementMargin))
}

@Composable
fun shortText(): String = stringResource(R.string.short_dummy_text)

@Composable
fun longText(): String = stringResource(R.string.long_dummy_text)