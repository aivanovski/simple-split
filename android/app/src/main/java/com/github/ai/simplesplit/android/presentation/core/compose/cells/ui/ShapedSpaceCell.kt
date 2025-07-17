package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.Dp
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ShapedSpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedSpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ElementSpace
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.toComposeShape

@Composable
fun ShapedSpaceCell(viewModel: ShapedSpaceCellViewModel) {
    val model = viewModel.model

    Card(
        shape = model.shape.toComposeShape(),
        colors = CardDefaults.cardColors(
            containerColor = AppTheme.theme.colors.cardPrimaryBackground
        ),
        modifier = Modifier
            .padding(
                horizontal = ElementMargin
            )
    ) {
        Box(
            modifier = Modifier
                .fillMaxWidth()
                .height(height = model.height)
        )
    }
}

@Composable
@Preview
fun ShapedSpaceCellPreview() {
    ThemedPreview(
        theme = LightTheme,
        background = LightTheme.colors.secondaryBackground
    ) {
        Column {
            ShapedSpaceCell(newShapedSpaceCellViewModel(height = GroupMargin))
            ElementSpace()
            ShapedSpaceCell(
                newShapedSpaceCellViewModel(
                    height = GroupMargin,
                    shape = CornersShape.TOP
                )
            )
            ShapedSpaceCell(
                newShapedSpaceCellViewModel(
                    height = GroupMargin,
                    shape = CornersShape.NONE
                )
            )
            ShapedSpaceCell(
                newShapedSpaceCellViewModel(
                    height = GroupMargin,
                    shape = CornersShape.BOTTOM
                )
            )
        }
    }
}

fun newShapedSpaceCellViewModel(
    height: Dp,
    shape: CornersShape = CornersShape.ALL
) = ShapedSpaceCellViewModel(
    model = ShapedSpaceCellModel(
        id = "id",
        height = height,
        shape = shape
    )
)