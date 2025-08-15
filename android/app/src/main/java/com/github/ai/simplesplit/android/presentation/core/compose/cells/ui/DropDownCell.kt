package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize.BODY_MEDIUM
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize.TITLE_MEDIUM
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DropDownCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.TwoLineItemHeight
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun DropDownCell(viewModel: DropDownCellViewModel) {
    val model by viewModel.observableModel.collectAsState()

    var isExpanded by remember { mutableStateOf(false) }

    val onOptionClick = rememberCallback { newOption: String ->
        viewModel.sendEvent(
            DropDownCellEvent.OnOptionSelect(
                cellId = model.id,
                selectedOption = newOption
            )
        )
    }

    Column(
        modifier = Modifier
            .fillMaxWidth()
    ) {
        Row(
            verticalAlignment = Alignment.CenterVertically,
            modifier = Modifier
                .fillMaxWidth()
                .clickable(
                    onClick = {
                        isExpanded = true
                    }
                )
                .padding(
                    horizontal = ElementMargin,
                    vertical = QuarterMargin
                )
                .defaultMinSize(minHeight = TwoLineItemHeight)
        ) {
            Column(
                modifier = Modifier
                    .weight(weight = 1f)
            ) {
                Text(
                    text = model.title,
                    color = AppTheme.theme.colors.primaryText,
                    style = TITLE_MEDIUM.toTextStyle()
                )

                if (model.selectedOption.isNotEmpty()) {
                    Text(
                        text = model.selectedOption,
                        color = AppTheme.theme.colors.secondaryText,
                        style = BODY_MEDIUM.toTextStyle()
                    )
                }
            }

            val icon = if (isExpanded) {
                AppIcon.ARROW_DROP_UP.vector
            } else {
                AppIcon.ARROW_DROP_DOWN.vector
            }

            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = AppTheme.theme.colors.primaryText
            )
        }

        DropdownMenu(
            expanded = isExpanded,
            onDismissRequest = { isExpanded = false },
            modifier = Modifier
                .fillMaxWidth()
        ) {
            model.options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onOptionClick.invoke(option)
                        isExpanded = false
                    }
                )
            }
        }
    }
}

@Preview
@Composable
fun DropDownCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            DropDownCell(newDropDownCell())
        }
    }
}

fun newDropDownCell() =
    DropDownCellViewModel(
        DropDownCellModel(
            id = "id",
            title = "Title",
            options = listOf("Option 1", "Option 2"),
            selectedOption = "Option 1"
        ),
        PreviewEventProvider
    )