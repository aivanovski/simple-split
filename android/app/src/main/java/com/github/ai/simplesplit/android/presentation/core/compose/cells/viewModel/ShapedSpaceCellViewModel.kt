package com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel

import androidx.compose.runtime.Immutable
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ShapedSpaceCellModel

@Immutable
class ShapedSpaceCellViewModel(
    override val model: ShapedSpaceCellModel
) : CellViewModel