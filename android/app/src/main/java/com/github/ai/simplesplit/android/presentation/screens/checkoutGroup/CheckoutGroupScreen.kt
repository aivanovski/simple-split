package com.github.ai.simplesplit.android.presentation.screens.checkoutGroup

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
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
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.AppTextField
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupIntent
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupState

@Composable
fun CheckoutGroupScreen(viewModel: CheckoutGroupViewModel) {
    val state by viewModel.state.collectAsState()

    CheckoutGroupScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun CheckoutGroupScreen(
    state: CheckoutGroupState,
    onIntent: (intent: CheckoutGroupIntent) -> Unit
) {
    val onBackClick = rememberOnClickedCallback {
        onIntent.invoke(CheckoutGroupIntent.OnBackClick)
    }

    val onMenuItemClick = rememberCallback { menuItem: TopBarMenuItem ->
        when (menuItem) {
            TopBarMenuItem.DONE -> onIntent.invoke(CheckoutGroupIntent.OnDoneClick)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.add_group_by_url),
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
                CheckoutGroupState.Loading -> {
                    CenteredBox {
                        CircularProgressIndicator()
                    }
                }

                is CheckoutGroupState.Data -> {
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
    state: CheckoutGroupState.Data,
    onIntent: (intent: CheckoutGroupIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(ElementMargin)
    ) {
        val onUrlChange = rememberCallback { newValue: String ->
            onIntent.invoke(CheckoutGroupIntent.OnUrlChanged(newValue))
        }

        AppTextField(
            value = state.url,
            error = state.urlError,
            label = stringResource(R.string.group_url),
            onValueChange = onUrlChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        // TODO: make error message more readable

        // Show error message if there's one
        state.errorMessage?.let { errorMessage ->
            Spacer(modifier = Modifier.height(SmallMargin))

            Text(
                text = errorMessage,
                color = AppTheme.theme.colors.errorText,
                style = AppTheme.theme.typography.bodySmall,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}