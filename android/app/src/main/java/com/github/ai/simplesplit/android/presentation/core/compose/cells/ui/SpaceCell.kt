package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.Dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel

@Composable
fun SpaceCell(viewModel: SpaceCellViewModel) {
    val model = viewModel.model

    Spacer(modifier = Modifier.height(height = model.height))
}

fun newSpaceCell(height: Dp = Dp.Unspecified) =
    SpaceCellViewModel(
        model = SpaceCellModel(
            id = "space_cell",
            height = height
        )
    )