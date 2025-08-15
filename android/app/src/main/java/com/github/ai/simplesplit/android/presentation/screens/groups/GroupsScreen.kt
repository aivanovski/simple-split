package com.github.ai.simplesplit.android.presentation.screens.groups

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.ai.simplesplit.android.BuildConfig
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.ui.GroupCell
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel.GroupCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsState

@Composable
fun GroupsScreen(viewModel: GroupsViewModel) {
    val state by viewModel.state.collectAsState()

    GroupsScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun GroupsScreen(
    state: GroupsState,
    onIntent: (intent: GroupsIntent) -> Unit
) {
    val onFabClick = rememberOnClickedCallback {
        onIntent.invoke(GroupsIntent.OnAddButtonClick)
    }
    val onMenuClick = rememberCallback { item: TopBarMenuItem ->
        onIntent.invoke(GroupsIntent.OnSettingsClick)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.groups),
                isBackVisible = false,
                menuItems = if (BuildConfig.DEBUG) {
                    listOf(TopBarMenuItem.SETTINGS)
                } else {
                    emptyList()
                },
                onMenuItemClick = onMenuClick
            )
        },
        floatingActionButton = {
            if (state != GroupsState.Loading) {
                FloatingActionButton(
                    onClick = onFabClick
                ) {
                    Icon(
                        imageVector = AppIcon.ADD.vector,
                        contentDescription = null
                    )
                }
            }
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
                GroupsState.Loading -> {
                    CenteredBox {
                        CircularProgressIndicator()
                    }
                }

                GroupsState.Empty -> {
                    CenteredBox {
                        Text(text = "No groups") // TODO: string
                    }
                }

                is GroupsState.Data -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.cellViewModels) { model ->
                            RenderCell(model)
                        }
                    }
                }

                is GroupsState.Error -> {
                    CenteredBox {
                        Text(text = state.message)
                    }
                }
            }
        }
    }
}

@Composable
private fun RenderCell(viewModel: CellViewModel) {
    when (viewModel) {
        is SpaceCellViewModel -> SpaceCell(viewModel)
        is GroupCellViewModel -> GroupCell(viewModel)
        else -> throw IllegalArgumentException("Unknown cell: $viewModel")
    }
}