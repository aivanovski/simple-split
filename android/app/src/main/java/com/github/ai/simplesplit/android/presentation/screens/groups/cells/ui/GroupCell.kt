package com.github.ai.simplesplit.android.presentation.screens.groups.cells.ui

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.sizeIn
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.constraintlayout.compose.ConstraintLayout
import androidx.constraintlayout.compose.Dimension
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.CardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupThreeLineItemHeight
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupTwoLineItemHeight
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel.GroupCellViewModel

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun GroupCell(viewModel: GroupCellViewModel) {
    val model = viewModel.model
    val isDescriptionVisible = model.description.isNotEmpty()

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(GroupCellEvent.OnClick(model.id))
    }

    val onLongClick = rememberOnClickedCallback {
        viewModel.sendEvent(GroupCellEvent.OnLongClick(model.id))
    }

    val minHeight = if (isDescriptionVisible) {
        GroupThreeLineItemHeight
    } else {
        GroupTwoLineItemHeight
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
        ConstraintLayout(
            modifier = Modifier
                .fillMaxWidth()
                .combinedClickable(
                    onClick = onClick,
                    onLongClick = onLongClick
                )
                .padding(horizontal = ElementMargin, vertical = ElementMargin)
                .sizeIn(minHeight = minHeight)
        ) {
            val (title, description, members, amount) = createRefs()

            Text(
                text = model.title,
                style = TextSize.TITLE_LARGE.toTextStyle(),
                color = AppTheme.theme.colors.primaryText,
                maxLines = 1,
                modifier = Modifier
                    .constrainAs(title) {
                        top.linkTo(parent.top)
                        start.linkTo(parent.start)
                        end.linkTo(amount.start)
                        width = Dimension.fillToConstraints
                    }
            )

            Text(
                text = model.amount,
                style = TextSize.TITLE_LARGE.toTextStyle(),
                color = AppTheme.theme.colors.secondaryText,
                maxLines = 1,
                modifier = Modifier
                    .constrainAs(amount) {
                        top.linkTo(parent.top)
                        end.linkTo(parent.end)
                    }
            )

            if (isDescriptionVisible) {
                Text(
                    text = model.description,
                    style = TextSize.BODY_MEDIUM.toTextStyle(),
                    color = AppTheme.theme.colors.primaryText,
                    maxLines = 1,
                    modifier = Modifier
                        .constrainAs(description) {
                            top.linkTo(title.bottom)
                            start.linkTo(parent.start)
                            end.linkTo(parent.end)
                            width = Dimension.fillToConstraints
                        }
                )
            }

            Text(
                text = model.members,
                style = TextSize.BODY_MEDIUM.toTextStyle(),
                color = AppTheme.theme.colors.secondaryText,
                maxLines = 1,
                modifier = Modifier
                    .constrainAs(members) {
                        if (isDescriptionVisible) {
                            top.linkTo(description.bottom)
                        } else {
                            top.linkTo(title.bottom, margin = QuarterMargin)
                        }
                        start.linkTo(parent.start)
                        end.linkTo(parent.end)
                        width = Dimension.fillToConstraints
                    }
            )
        }
    }
}

@Preview
@Composable
fun GroupCellPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            GroupCell(newGroupCell())
            ElementSpace()
            GroupCell(newGroupCell(description = ""))
        }
    }
}

fun newGroupCell(
    title: String = "Group",
    description: String = "Description",
    members: String = "Mickey Mouse, Donald Duck"
) = GroupCellViewModel(
    model = GroupCellModel(
        id = "id",
        title = title,
        description = description,
        members = members,
        amount = "100$"
    ),
    eventProvider = PreviewEventProvider
)