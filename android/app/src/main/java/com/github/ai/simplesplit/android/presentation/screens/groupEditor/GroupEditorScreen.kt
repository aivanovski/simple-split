package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import androidx.compose.foundation.layout.Box
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
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.compose.AppTextField
import com.github.ai.simplesplit.android.presentation.core.compose.CenteredBox
import com.github.ai.simplesplit.android.presentation.core.compose.ErrorMessageCard
import com.github.ai.simplesplit.android.presentation.core.compose.ErrorState
import com.github.ai.simplesplit.android.presentation.core.compose.TopBar
import com.github.ai.simplesplit.android.presentation.core.compose.TopBarMenuItem
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.rememberCallback
import com.github.ai.simplesplit.android.presentation.core.compose.rememberOnClickedCallback
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.MediumMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.SmallMargin
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorState
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.MemberItem

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
    val onMenuItemClick = rememberCallback { _: TopBarMenuItem ->
        onIntent.invoke(GroupEditorIntent.OnDoneClick)
    }

    Scaffold(
        topBar = {
            TopBar(
                title = stringResource(R.string.create_new_group_title),
                isBackVisible = true,
                onBackClick = onBackClick,
                menuItems = listOf(TopBarMenuItem.DONE),
                onMenuItemClick = onMenuItemClick
            )
        }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                )
        ) {
            when (state) {
                GroupEditorState.Loading -> {
                    CenteredBox {
                        CircularProgressIndicator()
                    }
                }

                is GroupEditorState.Error -> {
                    ErrorState(
                        error = state.error
                    )
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
    val onCloseErrorClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnCloseErrorClick)
    }
    val onApplyMemberEditClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnApplyMemberEditClick)
    }
    val onCancelMemberEditClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnCancelMemberEditClick)
    }
    val onAddClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnAddMemberClick)
    }
    val onTitleChange = rememberCallback { newTitle: String ->
        onIntent.invoke(GroupEditorIntent.OnTitleChanged(newTitle))
    }
    val onPasswordChange = rememberCallback { newPassword: String ->
        onIntent.invoke(GroupEditorIntent.OnPasswordChanged(newPassword))
    }
    val onConfirmPasswordChange = rememberCallback { newConfirmPassword: String ->
        onIntent.invoke(GroupEditorIntent.OnConfirmPasswordChanged(newConfirmPassword))
    }
    val onMemberChange = rememberCallback { newMember: String ->
        onIntent.invoke(GroupEditorIntent.OnMemberChanged(newMember))
    }
    val onPasswordToggleClick = rememberCallback { isVisible: Boolean ->
        onIntent.invoke(GroupEditorIntent.OnPasswordToggleClick(isVisible))
    }
    val onConfirmPasswordToggleClick = rememberCallback { isVisible: Boolean ->
        onIntent.invoke(GroupEditorIntent.OnConfirmPasswordToggleClick(isVisible))
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(state = rememberScrollState())
            .padding(ElementMargin)
    ) {
        if (state.error != null) {
            ErrorMessageCard(
                error = state.error,
                onClose = onCloseErrorClick
            )
        }

        AppTextField(
            value = state.title,
            error = state.titleError,
            label = stringResource(R.string.group_name),
            onValueChange = onTitleChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.password,
            error = state.passwordError,
            label = stringResource(R.string.password),
            isPasswordToggleEnabled = true,
            isPasswordVisible = state.isPasswordVisible,
            onPasswordToggleClicked = onPasswordToggleClick,
            onValueChange = onPasswordChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        AppTextField(
            value = state.confirmPassword,
            error = state.confirmPasswordError,
            label = stringResource(R.string.confirm_password),
            isPasswordToggleEnabled = true,
            isPasswordVisible = state.isConfirmPasswordVisible,
            onPasswordToggleClicked = onConfirmPasswordToggleClick,
            onValueChange = onConfirmPasswordChange,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(SmallMargin))

        Row(
            modifier = Modifier.fillMaxWidth()
        ) {
            AppTextField(
                value = state.member,
                error = state.memberError,
                label = stringResource(R.string.member_name),
                onValueChange = onMemberChange,
                modifier = Modifier.weight(1f)
            )

            Spacer(modifier = Modifier.width(width = HalfMargin))

            if (state.isApplyButtonVisible) {
                IconButton(
                    onClick = onApplyMemberEditClick,
                    modifier = Modifier.padding(top = MediumMargin)
                ) {
                    Icon(
                        imageVector = AppIcon.CHECK.vector,
                        contentDescription = null
                    )
                }
            }

            if (state.isAddButtonVisible) {
                IconButton(
                    onClick = onAddClick,
                    modifier = Modifier.padding(top = MediumMargin)
                ) {
                    Icon(
                        imageVector = AppIcon.ADD.vector,
                        contentDescription = null
                    )
                }
            }

            if (state.isCancelButtonVisible) {
                IconButton(
                    onClick = onCancelMemberEditClick,
                    modifier = Modifier.padding(top = MediumMargin)
                ) {
                    Icon(
                        imageVector = AppIcon.CLOSE.vector,
                        contentDescription = null
                    )
                }
            }
        }

        for ((index, member) in state.members.withIndex()) {
            if (index > 0) {
                Spacer(modifier = Modifier.height(SmallMargin))
            }

            MemberItemComposable(
                member = member,
                index = index,
                onIntent = onIntent
            )
        }
    }
}

@Composable
private fun MemberItemComposable(
    member: MemberItem,
    index: Int,
    onIntent: (intent: GroupEditorIntent) -> Unit
) {
    val onEditMemberClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnEditMemberClick(index))
    }
    val onRemoveMemberClick = rememberOnClickedCallback {
        onIntent.invoke(GroupEditorIntent.OnRemoveMemberClick(index))
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
                text = member.name,
                style = MaterialTheme.typography.bodyMedium,
                modifier = Modifier
                    .weight(1f)
                    .padding(start = ElementMargin)
            )

            IconButton(
                onClick = onEditMemberClick
            ) {
                Icon(
                    imageVector = AppIcon.EDIT.vector,
                    contentDescription = null
                )
            }

            IconButton(
                onClick = onRemoveMemberClick
            ) {
                Icon(
                    imageVector = AppIcon.CLOSE.vector,
                    contentDescription = null
                )
            }
        }
    }
}

@Preview
@Composable
fun GroupEditorScreenDataPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        GroupEditorScreen(
            state = newDataState(),
            onIntent = {}
        )
    }
}

private fun newDataState() =
    GroupEditorState.Data(
        members = listOf(
            MemberItem("Donald"),
            MemberItem("Mickey")
        ),
        isApplyButtonVisible = true,
        isAddButtonVisible = true,
        isCancelButtonVisible = true
    )