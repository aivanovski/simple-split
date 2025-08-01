package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.TextSquaredChip
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin
import com.github.ai.simplesplit.android.presentation.core.compose.toComposeShape
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.ExpenseCellViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun ExpenseCell(viewModel: ExpenseCellViewModel) {
    val model = viewModel.model

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(ExpenseCellEvent.OnClick(model.id))
    }

    val onLongClick = rememberOnClickedCallback {
        viewModel.sendEvent(ExpenseCellEvent.OnLongClick(model.id))
    }

    Card(
        shape = model.shape.toComposeShape(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.cardPrimaryBackground
        ),
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ElementMargin
            )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(ElementMargin)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = model.title,
                    style = TextSize.BODY_LARGE.toTextStyle(),
                    fontWeight = FontWeight.Bold,
                    color = AppTheme.theme.colors.primaryText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = model.amount,
                    style = TextSize.BODY_LARGE.toTextStyle(),
                    color = AppTheme.theme.colors.primaryText,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(QuarterMargin))

            Text(
                text = model.description,
                style = TextSize.BODY_MEDIUM.toTextStyle(),
                color = AppTheme.theme.colors.primaryText,
                maxLines = 1,
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                verticalAlignment = Alignment.CenterVertically,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(top = QuarterMargin)
            ) {
                for (member in model.members) {
                    TextSquaredChip(
                        text = member
                    )
                }

                Text(
                    text = model.date,
                    textAlign = TextAlign.End,
                    style = TextSize.BODY_MEDIUM.toTextStyle(),
                    color = AppTheme.theme.colors.secondaryText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )
            }
        }
    }
}

@Preview
@Composable
fun ExpenseCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            ExpenseCell(newExpenseCell())
        }
    }
}

private fun newExpenseCell(shape: CornersShape = CornersShape.ALL) =
    ExpenseCellViewModel(
        model = ExpenseCellModel(
            id = "id",
            title = "Lunch",
            description = "Paid by: John",
            members = listOf("Jh", "Jj", "B"),
            amount = "45.50$",
            date = "27 Feb",
            shape = shape
        ),
        eventProvider = PreviewEventProvider
    )