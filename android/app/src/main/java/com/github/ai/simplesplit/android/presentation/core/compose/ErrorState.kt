package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme

@Composable
fun ErrorState(
    error: ErrorMessage,
    onAction: ((actionId: Int) -> Unit)? = null
) {
    Column(
        verticalArrangement = Arrangement.Center,
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier
            .fillMaxSize()
            .padding(all = GroupMargin)
    ) {
        Box(
            contentAlignment = Alignment.Center,
            modifier = Modifier
                .defaultMinSize(minHeight = 64.dp)
        ) {
            Text(
                text = error.message,
                style = TextSize.TITLE_MEDIUM.toTextStyle(),
                color = TextColor.ERROR.toColor(),
                textAlign = TextAlign.Center,
                modifier = Modifier
                    .fillMaxWidth()
            )
        }

        if (error.actionText.isNotEmpty()) {
            val onActionClick = rememberOnClickedCallback {
                if (error.actionId != null) {
                    onAction?.invoke(error.actionId)
                }
            }

            Button(
                colors = ButtonDefaults.buttonColors(
                    containerColor = AppTheme.theme.colors.errorText
                ),
                onClick = onActionClick,
                modifier = Modifier
            ) {
                Text(error.actionText)
            }
        }
    }
}

@Preview
@Composable
fun ErrorStatePreview() {
    ThemedScreenPreview(theme = LightTheme) {
        ErrorState(
            error = newErrorMessage(),
            onAction = {}
        )
    }
}