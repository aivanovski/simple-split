package com.github.ai.simplesplit.android.presentation.screens.groups.cells.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.CardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel.GroupCellViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupCell(viewModel: GroupCellViewModel) {
    val model = viewModel.model

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(GroupCellEvent.OnClick(model.id))
    }

    val onLongClick = rememberOnClickedCallback {
        viewModel.sendEvent(GroupCellEvent.OnLongClick(model.id))
    }

    Card(
        shape = RoundedCornerShape(size = CardCornerSize),
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
                    style = AppTheme.theme.typography.headlineSmall,
                    color = AppTheme.theme.colors.primaryText,
                    maxLines = 1,
                    modifier = Modifier.weight(1f)
                )

                Text(
                    text = model.amount,
                    style = AppTheme.theme.typography.headlineSmall,
                    color = AppTheme.theme.colors.secondaryText,
                    maxLines = 1
                )
            }

            Spacer(modifier = Modifier.height(QuarterMargin))

            if (model.description.isNotEmpty()) {
                Text(
                    text = model.description,
                    style = AppTheme.theme.typography.bodyMedium,
                    color = AppTheme.theme.colors.primaryText,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }

            if (model.members.isNotEmpty()) {
                Text(
                    text = model.members,
                    style = AppTheme.theme.typography.bodyMedium,
                    color = AppTheme.theme.colors.secondaryText,
                    maxLines = 1,
                    modifier = Modifier.fillMaxWidth()
                )
            }
        }
    }
}

@Preview
@Composable
fun GroupCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            GroupCell(newGroupCell())
        }
    }
}

private fun newGroupCell() =
    GroupCellViewModel(
        model = GroupCellModel(
            id = "id",
            title = "Title",
            description = "Description",
            members = "Mickey Mouse, Donald Duck",
            amount = "100$"
        ),
        eventProvider = PreviewEventProvider
    )