package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.BoxWithConstraints
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.AppTextField
import com.github.ai.simplesplit.android.presentation.core.compose.EmptyState
import com.github.ai.simplesplit.android.presentation.core.compose.ErrorState
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.DividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.MenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newDividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newMenuCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.newSpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.DialogCardCornerSize
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogIntent
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogState

@Composable
fun SelectCurrencyDialogScreen(viewModel: SelectCurrencyDialogViewModel) {
    val state by viewModel.state.collectAsState()

    SelectCurrencyDialogScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun SelectCurrencyDialogScreen(
    state: SelectCurrencyDialogState,
    onIntent: (intent: SelectCurrencyDialogIntent) -> Unit
) {
    val onQueryTextChange = rememberCallback { query: String ->
        onIntent.invoke(SelectCurrencyDialogIntent.OnQueryTextChange(query))
    }
    val onResetQueryClick = rememberOnClickedCallback {
        onIntent.invoke(SelectCurrencyDialogIntent.OnResetQueryClick)
    }
    val onCloseIconClick = rememberOnClickedCallback {
        onIntent.invoke(SelectCurrencyDialogIntent.Dismiss)
    }

    BoxWithConstraints(
        modifier = Modifier
    ) {
        val minContentHeight = maxHeight * 0.7f

        Card(
            shape = RoundedCornerShape(DialogCardCornerSize, DialogCardCornerSize, 0.dp, 0.dp),
            colors = CardDefaults.cardColors(
                containerColor = AppTheme.theme.colors.secondaryBackground
            )
        ) {
            when (state) {
                is SelectCurrencyDialogState.Error -> {
                    ErrorState(state.message)
                }

                is SelectCurrencyDialogState.Data -> {
                    Column(
                        modifier = Modifier
                            .fillMaxWidth()
                    ) {
                        Row(
                            verticalAlignment = Alignment.CenterVertically,
                            modifier = Modifier
                                .fillMaxWidth()
                                .verticalScroll(rememberScrollState())
                        ) {
                            AppTextField(
                                value = state.query,
                                label = stringResource(R.string.search_or_select_currency),
                                isResetIconEnabled = true,
                                onResetIconClick = onResetQueryClick,
                                onValueChange = onQueryTextChange,
                                modifier = Modifier
                                    .weight(weight = 1f)
                                    .padding(
                                        horizontal = ElementMargin,
                                        vertical = ElementMargin
                                    )
                            )

                            Box(
                                modifier = Modifier
                                    .padding(end = HalfMargin)
                                    .size(48.dp)
                                    .clickable(onClick = onCloseIconClick)
                            ) {
                                Icon(
                                    imageVector = AppIcon.CLOSE.vector,
                                    tint = AppTheme.theme.colors.primaryIcon,
                                    contentDescription = null,
                                    modifier = Modifier
                                        .align(Alignment.Center)
                                )
                            }
                        }

                        when {
                            state.isLoading -> {
                                Box(
                                    contentAlignment = Alignment.Center,
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .height(height = 200.dp)
                                ) {
                                    CircularProgressIndicator()
                                }
                            }

                            state.cellViewModels.isEmpty() -> {
                                Box(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = minContentHeight)
                                ) {
                                    EmptyState(
                                        text = stringResource(R.string.no_results_found),
                                        modifier = Modifier
                                            .fillMaxWidth()
                                            .padding(top = 200.dp)
                                    )
                                }
                            }

                            else -> {
                                LazyColumn(
                                    modifier = Modifier
                                        .fillMaxWidth()
                                        .heightIn(min = minContentHeight)
                                ) {
                                    items(state.cellViewModels) { cellViewModel ->
                                        when (cellViewModel) {
                                            is SpaceCellViewModel -> SpaceCell(cellViewModel)
                                            is DividerCellViewModel -> DividerCell(cellViewModel)
                                            is MenuCellViewModel -> MenuCell(cellViewModel)
                                            else -> throw IllegalArgumentException(
                                                "Unknown cell type: ${cellViewModel::class.simpleName}"
                                            )
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Preview(showBackground = true)
@Composable
private fun SelectCurrencyDialogScreenPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        SelectCurrencyDialogScreen(
            state = SelectCurrencyDialogState.Data(
                query = "",
                cellViewModels = listOf(
                    newSpaceCell(
                        height = HalfMargin
                    ),
                    newDividerCell(),
                    newSpaceCell(
                        height = ElementMargin
                    ),
                    newMenuCell(
                        icon = AppIcon.CHECK.vector,
                        title = "USD - US Dollar"
                    ),
                    newSpaceCell(
                        height = ElementMargin
                    ),
                    newMenuCell(
                        icon = AppIcon.CHECK.vector,
                        title = "EUR - Euro"
                    ),
                    newSpaceCell(
                        height = ElementMargin
                    ),
                    newMenuCell(
                        icon = AppIcon.CHECK.vector,
                        title = "GBP - British Pound"
                    )
                )
            ),
            onIntent = {}
        )
    }
}

@Preview
@Composable
private fun SelectCurrencyDialogScreenEmptyPreview() {
    ThemedPreview(
        theme = LightTheme
    ) {
        SelectCurrencyDialogScreen(
            state = SelectCurrencyDialogState.Data(
                query = "",
                cellViewModels = emptyList()
            ),
            onIntent = {}
        )
    }
}