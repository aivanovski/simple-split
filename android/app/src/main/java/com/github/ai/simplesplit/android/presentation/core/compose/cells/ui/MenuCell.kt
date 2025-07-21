package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.OneLineItemHeight

@Composable
fun MenuCell(viewModel: MenuCellViewModel) {
    val model = viewModel.model

    val onClick = rememberOnClickedCallback {
        viewModel.sendEvent(MenuCellEvent.OnClick(model.id))
    }

    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(horizontal = ElementMargin)
            .height(height = OneLineItemHeight)
    ) {
        Icon(
            imageVector = model.icon,
            tint = AppTheme.theme.colors.primaryIcon,
            contentDescription = null
        )

        Text(
            text = model.title,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
            color = AppTheme.theme.colors.primaryText,
            style = AppTheme.theme.typography.bodyLarge,
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = HalfMargin)
        )
    }
}

@Preview
@Composable
fun MenuCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            MenuCell(newMenuCell())
        }
    }
}

fun newMenuCell(
    icon: ImageVector = AppIcons.Settings,
    title: String = "Settings"
) = MenuCellViewModel(
    model = MenuCellModel(
        id = "id",
        icon = icon,
        title = title
    ),
    eventProvider = PreviewEventProvider
)