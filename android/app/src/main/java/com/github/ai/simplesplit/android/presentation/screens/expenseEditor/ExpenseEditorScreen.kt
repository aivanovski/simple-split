package com.github.ai.simplesplit.android.presentation.screens.expenseEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.AppDropdownField
import com.github.ai.simplesplit.android.presentation.core.compose.AppTextField
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorState

@Composable
fun ExpenseEditorScreen(viewModel: ExpenseEditorViewModel) {
    val state by viewModel.state.collectAsState()

    ExpenseEditorScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun ExpenseEditorScreen(
    state: ExpenseEditorState,
    onIntent: (intent: ExpenseEditorIntent) -> Unit
) {
    val onBackClick = rememberOnClickedCallback {
        onIntent.invoke(ExpenseEditorIntent.OnBackClick)
    }

    val onMenuItemClick = rememberCallback { _: TopBarMenuItem ->
        onIntent.invoke(ExpenseEditorIntent.OnDoneClick)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.add_expense),
                isBackVisible = true,
                onBackClick = onBackClick,
                menuItems = listOf(TopBarMenuItem.DONE),
                onMenuItemClick = onMenuItemClick
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
                ExpenseEditorState.Loading -> {
                    CenteredBox {
                        CircularProgressIndicator()
                    }
                }

                is ExpenseEditorState.Error -> {
                    CenteredBox {
                        Text(text = state.message)
                    }
                }

                is ExpenseEditorState.Data -> {
                    RenderDataContent(
                        state = state,
                        onIntent = onIntent
                    )
                }
            }
        }
    }
}

@Composable
private fun RenderDataContent(
    state: ExpenseEditorState.Data,
    onIntent: (intent: ExpenseEditorIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(ElementMargin)
    ) {
        AppDropdownField(
            value = state.payer,
            label = stringResource(R.string.payer),
            options = state.availablePayers,
            onValueChange = { newValue ->
                onIntent.invoke(ExpenseEditorIntent.OnPayerChanged(newValue))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.title,
            error = state.titleError,
            label = stringResource(R.string.expense_title),
            onValueChange = { newValue ->
                onIntent.invoke(ExpenseEditorIntent.OnTitleChanged(newValue))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.amount,
            error = state.amountError,
            label = stringResource(R.string.amount),
            onValueChange = { newValue ->
                onIntent.invoke(ExpenseEditorIntent.OnAmountChanged(newValue))
            },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Preview
@Composable
fun ExpenseEditorDataPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        ExpenseEditorScreen(
            state = newDataState(),
            onIntent = {}
        )
    }
}

private fun newDataState() =
    ExpenseEditorState.Data(
        availablePayers = listOf("John Doe", "Jane Smith", "Bob Johnson")
    )