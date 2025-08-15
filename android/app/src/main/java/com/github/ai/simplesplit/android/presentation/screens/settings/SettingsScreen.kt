package com.github.ai.simplesplit.android.presentation.screens.settings

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.DropDownCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SwitchCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DropDownCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SwitchCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.screens.settings.model.SettingsIntent
import com.github.ai.simplesplit.android.presentation.screens.settings.model.SettingsState

@Composable
fun SettingsScreen(viewModel: SettingsViewModel) {
    val state by viewModel.state.collectAsState()

    SettingsScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun SettingsScreen(
    state: SettingsState,
    onIntent: (intent: SettingsIntent) -> Unit
) {
    val onBackClick = rememberOnClickedCallback {
        onIntent.invoke(SettingsIntent.OnBackClick)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.settings),
                isBackVisible = true,
                onBackClick = onBackClick
            )
        }
    ) { padding ->

        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            color = AppTheme.theme.colors.background
        ) {
            when (state) {
                SettingsState.Loading -> {
                    CenteredBox { CircularProgressIndicator() }
                }

                is SettingsState.Data -> {
                    LazyColumn(
                        modifier = Modifier
                            .fillMaxSize()
                            .padding()
                    ) {
                        items(state.cellViewModels) { model ->
                            RenderCell(model)
                        }
                    }
                }

                is SettingsState.Error -> {
                    CenteredBox { Text(text = state.message) }
                }
            }
        }
    }
}

@Composable
private fun RenderCell(viewModel: CellViewModel) {
    when (viewModel) {
        is SpaceCellViewModel -> SpaceCell(viewModel)
        is SwitchCellViewModel -> SwitchCell(viewModel)
        is DropDownCellViewModel -> DropDownCell(viewModel)
        else -> throw IllegalArgumentException("Unknown cell: $viewModel")
    }
}