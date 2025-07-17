package com.github.ai.simplesplit.android.presentation.core.compose.cells.ui

import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.height
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel

@Composable
fun SpaceCell(viewModel: SpaceCellViewModel) {
    val model = viewModel.model

    Spacer(modifier = Modifier.height(height = model.height))
}