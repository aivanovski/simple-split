package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
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
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.DividerCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.EmptyMessageCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.HeaderCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.ShapedSpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.ShapedTextCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.ui.SpaceCell
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.EmptyMessageCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.HeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedSpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedTextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.ui.ExpenseCell
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.ui.SettlementCell
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.ExpenseCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.SettlementCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsIntent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsState
import com.github.ai.simplesplit.android.utils.StringUtils

@Composable
fun GroupDetailsScreen(viewModel: GroupDetailsViewModel) {
    val state by viewModel.state.collectAsState()

    GroupDetailsScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun GroupDetailsScreen(
    state: GroupDetailsState,
    onIntent: (intent: GroupDetailsIntent) -> Unit
) {
    val onBackClick = rememberOnClickedCallback {
        onIntent.invoke(GroupDetailsIntent.OnBackClick)
    }

    val onFabClick = rememberOnClickedCallback {
        onIntent.invoke(GroupDetailsIntent.OnFabClick)
    }

    val onMenuItemClick = rememberCallback { _: TopBarMenuItem ->
        onIntent.invoke(GroupDetailsIntent.OnMenuClick)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = StringUtils.EMPTY,
                isBackVisible = true,
                onBackClick = onBackClick,
                menuItems = listOf(TopBarMenuItem.MENU),
                onMenuItemClick = onMenuItemClick
            )
        },
        floatingActionButton = {
            if (state != GroupDetailsState.Loading) {
                FloatingActionButton(
                    onClick = onFabClick
                ) {
                    Icon(
                        imageVector = Icons.Default.Add,
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
                GroupDetailsState.Loading -> {
                    CenteredBox { CircularProgressIndicator() }
                }

                is GroupDetailsState.Data -> {
                    LazyColumn(
                        modifier = Modifier.fillMaxSize()
                    ) {
                        items(state.cellViewModels) { model ->
                            RenderCell(model)
                        }
                    }
                }

                is GroupDetailsState.Error -> {
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
        is ShapedSpaceCellViewModel -> ShapedSpaceCell(viewModel)
        is ShapedTextCellViewModel -> ShapedTextCell(viewModel)
        is HeaderCellViewModel -> HeaderCell(viewModel)
        is ExpenseCellViewModel -> ExpenseCell(viewModel)
        is SettlementCellViewModel -> SettlementCell(viewModel)
        is DividerCellViewModel -> DividerCell(viewModel)
        is EmptyMessageCellViewModel -> EmptyMessageCell(viewModel)
    }
}

@Preview
@Composable
fun GroupDetailsDataPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        GroupDetailsScreen(
            state = newDataState(),
            onIntent = {}
        )
    }
}

private fun newDataState() =
    GroupDetailsState.Data(
        cellViewModels = listOf()
    )