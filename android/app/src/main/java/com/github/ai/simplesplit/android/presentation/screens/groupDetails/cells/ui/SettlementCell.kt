package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.DividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newDividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toComposeShape
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.SettlementCellViewModel

@Composable
fun SettlementCell(viewModel: SettlementCellViewModel) {
    val model = viewModel.model

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(SettlementCellEvent.OnClick(model.id))
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
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = onClick
                )
                .padding(ElementMargin),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = model.title,
                style = TextSize.BODY_LARGE.toTextStyle(),
                color = AppTheme.theme.colors.primaryText,
                modifier = Modifier.weight(1f)
            )

            Text(
                text = model.amount,
                style = TextSize.BODY_LARGE.toTextStyle(),
                color = AppTheme.theme.colors.secondaryText,
                maxLines = 1
            )
        }
    }
}

@Preview
@Composable
fun SettlementCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            SettlementCell(
                newSettlementCell(
                    shape = CornersShape.TOP
                )
            )
            DividerCell(newDividerCell())
            SettlementCell(
                newSettlementCell(
                    text = "JohnJohnJohnJohn → JaneJaneJaneJaneJaneJane",
                    shape = CornersShape.BOTTOM
                )
            )
        }
    }
}

private fun newSettlementCell(
    text: String = "John → Jane",
    shape: CornersShape = CornersShape.ALL
) = SettlementCellViewModel(
    model = SettlementCellModel(
        id = "settlement_id",
        title = text,
        amount = "25.00$",
        shape = shape
    ),
    eventProvider = PreviewEventProvider
)