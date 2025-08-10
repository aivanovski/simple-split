package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.AppTextField
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorState

@Composable
fun GroupEditorScreen(viewModel: GroupEditorViewModel) {
    val state by viewModel.state.collectAsState()

    GroupEditorScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun GroupEditorScreen(
    state: GroupEditorState,
    onIntent: (intent: GroupEditorIntent) -> Unit
) {
    val onBackClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnBackClick)
    }

    val onMenuItemClick = rememberCallback { menuItem: TopBarMenuItem ->
        when (menuItem) {
            TopBarMenuItem.DONE -> onIntent.invoke(GroupEditorIntent.OnDoneClick)
        }
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.create_new_group_title),
                isBackVisible = true,
                onBackClick = onBackClick,
                menuItems = listOf(TopBarMenuItem.DONE), // TODO: move to state
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
                GroupEditorState.Loading -> {
                    CenteredBox {
                        CircularProgressIndicator()
                    }
                }

                is GroupEditorState.Error -> {
                    CenteredBox {
                        Text(text = state.message)
                    }
                }

                is GroupEditorState.Data -> {
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
    state: GroupEditorState.Data,
    onIntent: (intent: GroupEditorIntent) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(ElementMargin)
    ) {
        AppTextField(
            value = state.title,
            error = state.titleError,
            label = stringResource(R.string.group_name),
            // TODO: remember callback
            onValueChange = { newValue ->
                onIntent.invoke(GroupEditorIntent.OnTitleChanged(newValue))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.password,
            error = state.passwordError,
            label = stringResource(R.string.password),
            isPasswordToggleEnabled = true,
            isPasswordVisible = state.isPasswordVisible,
            // TODO: remember callbacks
            onPasswordToggleClicked = { isVisible ->
                onIntent.invoke(GroupEditorIntent.OnPasswordToggleClick(isVisible))
            },
            onValueChange = { newValue ->
                onIntent.invoke(GroupEditorIntent.OnPasswordChanged(newValue))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.confirmPassword,
            error = state.confirmPasswordError,
            label = stringResource(R.string.confirm_password),
            isPasswordToggleEnabled = true,
            isPasswordVisible = state.isConfirmPasswordVisible,
            // TODO: remember callbacks
            onPasswordToggleClicked = { isVisible ->
                onIntent.invoke(GroupEditorIntent.OnConfirmPasswordToggleClick(isVisible))
            },
            onValueChange = { newValue ->
                onIntent.invoke(GroupEditorIntent.OnConfirmPasswordChanged(newValue))
            },
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            AppTextField(
                value = state.member,
                error = state.memberError,
                label = stringResource(R.string.member_name),
                // TODO: remember callback
                onValueChange = { newValue ->
                    onIntent.invoke(GroupEditorIntent.OnMemberChanged(newValue))
                },
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(width = HalfMargin))

            IconButton(
                // TODO: remember callback
                onClick = {
                    onIntent.invoke(GroupEditorIntent.OnAddMemberClick)
                }
            ) {
                Icon(
                    imageVector = AppIcons.Add,
                    contentDescription = null
                )
            }
        }

        for ((index, member) in state.members.withIndex()) {
            if (index > 0) {
                Spacer(modifier = Modifier.height(SmallMargin))
            }

            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = AppTheme.theme.colors.cardSecondaryBackground
                )
            ) {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text(
                        text = member,
                        style = MaterialTheme.typography.bodyMedium,
                        modifier = Modifier
                            .weight(1f)
                            .padding(start = ElementMargin)
                    )

                    IconButton(
                        // TODO: remember callback
                        onClick = { onIntent.invoke(GroupEditorIntent.OnRemoveMemberClick(index)) }
                    ) {
                        Icon(
                            imageVector = AppIcons.Close,
                            contentDescription = null
                        )
                    }
                }
            }
        }
    }
}