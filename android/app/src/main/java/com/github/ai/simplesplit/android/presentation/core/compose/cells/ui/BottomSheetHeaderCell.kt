package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material3.Icon
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.BottomSheetHeaderCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.BottomSheetHeaderCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.BottomSheetHeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.PreviewEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DoubleGroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toTextStyle

@Composable
fun BottomSheetHeaderCell(viewModel: BottomSheetHeaderCellViewModel) {
    val model = viewModel.model

    val onIconClick = rememberOnClickedCallback {
        viewModel.sendEvent(BottomSheetHeaderCellEvent.OnIconClick(model.id))
    }

    Box(
        modifier = Modifier
            .fillMaxWidth()
            .padding(
                start = ElementMargin
            )
    ) {
        Box(
            modifier = Modifier
                .align(Alignment.CenterEnd)
                .padding(end = HalfMargin)
                .size(48.dp)
                .clickable(onClick = onIconClick)
        ) {
            Icon(
                imageVector = model.icon,
                tint = AppTheme.theme.colors.primaryIcon,
                contentDescription = null,
                modifier = Modifier
                    .align(Alignment.Center)
            )
        }

        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(end = DoubleGroupMargin)
        ) {
            Text(
                text = model.title,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.theme.colors.primaryText,
                style = model.titleTextSize.toTextStyle()
            )

            Text(
                text = model.description,
                overflow = TextOverflow.Ellipsis,
                color = AppTheme.theme.colors.primaryText,
                style = model.descriptionTextSize.toTextStyle()
            )
        }
    }
}

@Preview
@Composable
fun BottomSheetHeaderCellPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        Column {
            BottomSheetHeaderCell(newBottomSheetHeaderCell())
        }
    }
}

fun newBottomSheetHeaderCell(
    icon: ImageVector = AppIcons.Close,
    title: String = "Beer",
    description: String = "15.42$",
    titleTextSize: TextSize = TextSize.TITLE_MEDIUM,
    descriptionTextSize: TextSize = TextSize.TITLE_LARGE
) = BottomSheetHeaderCellViewModel(
    model = BottomSheetHeaderCellModel(
        id = "id",
        icon = icon,
        title = title,
        description = description,
        titleTextSize = titleTextSize,
        descriptionTextSize = descriptionTextSize
    ),
    eventProvider = PreviewEventProvider
)