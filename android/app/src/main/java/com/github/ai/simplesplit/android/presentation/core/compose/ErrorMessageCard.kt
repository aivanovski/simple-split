package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.CardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin

@Composable
fun ErrorMessageCard(
    error: ErrorMessage,
    onClose: () -> Unit,
    onAction: ((actionId: Int) -> Unit)? = null
) {
    Card(
        shape = RoundedCornerShape(size = CardCornerSize),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.cardPrimaryBackground
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = GroupMargin,
                end = GroupMargin,
                bottom = ElementMargin
            )
    ) {
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .padding(start = GroupMargin, end = HalfMargin)
                .defaultMinSize(minHeight = 96.dp)
        ) {
            val (message, closeIcon, actionButton) = createRefs()

            val isActionButtonVisible = error.actionText.isNotEmpty()

            Box(
                modifier = Modifier
                    .constrainAs(closeIcon) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
                    .clickable(
                        onClick = onClose
                    )
            ) {
                Icon(
                    imageVector = AppIcon.CLOSE.vector,
                    contentDescription = null,
                    tint = AppTheme.theme.colors.errorText,
                    modifier = Modifier
                        .padding(HalfMargin)
                )
            }

            Box(
                contentAlignment = Alignment.Center,
                modifier = Modifier
                    .defaultMinSize(minHeight = 64.dp)
                    .constrainAs(message) {
                        top.linkTo(closeIcon.bottom)
                        start.linkTo(parent.start)
                        end.linkTo(closeIcon.start)
                        if (!isActionButtonVisible) {
                            (
                                bottom.linkTo(parent.bottom, margin = GroupMargin)
                                )
                        }
                        width = Dimension.fillToConstraints
                    }
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
                        .constrainAs(actionButton) {
                            end.linkTo(parent.end)
                            top.linkTo(message.bottom, margin = QuarterMargin)
                            bottom.linkTo(parent.bottom, margin = ElementMargin)
                        }
                ) {
                    Text(error.actionText)
                }
            }
        }
    }
}

@Preview
@Composable
fun ErrorMessagePreview() {
    ThemedScreenPreview(theme = LightTheme) {
        Column {
            ErrorMessageCard(
                error = newErrorMessage(),
                onClose = {},
                onAction = {}
            )

            ElementSpace()

            ErrorMessageCard(
                error = newErrorMessage(actionText = ""),
                onClose = {},
                onAction = {}
            )
            CenteredBox {
                Text("Screen Content")
            }
        }
    }
}

@Composable
fun newErrorMessage(
    message: String = stringResource(R.string.medium_dummy_text),
    actionText: String = "Retry"
) = ErrorMessage(
    message = message,
    actionText = actionText
)