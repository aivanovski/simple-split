package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.defaultMinSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SwitchCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.QuarterMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.TwoLineItemHeight

@Composable
fun SwitchCell(viewModel: SwitchCellViewModel) {
    val model by viewModel.observableModel.collectAsState()

    val onChecked = rememberCallback { isChecked: Boolean ->
        viewModel.sendIntent(
            SwitchCellEvent.OnCheckChanged(
                cellId = model.id,
                isChecked = isChecked
            )
        )
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                horizontal = ElementMargin,
                vertical = QuarterMargin
            )
            .defaultMinSize(minHeight = TwoLineItemHeight)
    ) {
        Column(
            modifier = Modifier
                .weight(weight = 1f)
                .padding(end = SmallMargin)
        ) {
            Text(
                text = model.title,
                color = AppTheme.theme.colors.primaryText,
                style = AppTheme.theme.typography.titleMedium
            )

            if (model.description.isNotEmpty()) {
                Text(
                    text = model.description,
                    color = AppTheme.theme.colors.secondaryText,
                    style = AppTheme.theme.typography.bodyMedium
                )
            }
        }

        Switch(
            checked = model.isChecked,
            enabled = model.isEnabled,
            onCheckedChange = onChecked
        )
    }
}

@Composable
@Preview
fun SwitchCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            SwitchCell(newSwitchCell(isChecked = true, isEnabled = true))
            ElementSpace()
            SwitchCell(newSwitchCell(isChecked = true, isEnabled = false))
            ElementSpace()
            SwitchCell(newSwitchCell(isChecked = false, isEnabled = true))
            ElementSpace()
            SwitchCell(newSwitchCell(isChecked = false, isEnabled = false))
        }
    }
}

fun newSwitchCell(
    title: String = "Title",
    description: String = "Description",
    isChecked: Boolean = false,
    isEnabled: Boolean = true
) = SwitchCellViewModel(
    model = SwitchCellModel(
        id = "id",
        title = title,
        description = description,
        isChecked = isChecked,
        isEnabled = isEnabled
    ),
    eventProvider = PreviewEventProvider
)