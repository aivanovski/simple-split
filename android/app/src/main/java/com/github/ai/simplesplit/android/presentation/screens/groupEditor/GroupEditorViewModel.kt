package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.MviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorState
import com.github.ai.simplesplit.android.utils.StringUtils
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.singleFlowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class GroupEditorViewModel(
    private val interactor: GroupEditorInteractor,
    private val resourceProvider: ResourceProvider,
    private val router: Router,
    private val args: GroupEditorArgs
) : MviViewModel<GroupEditorState, GroupEditorIntent>(
    initialState = GroupEditorState.Loading,
    initialIntent = GroupEditorIntent.Initialize
) {

    private var dataState by mutableStateFlow(GroupEditorState.Data())

    override fun handleIntent(intent: GroupEditorIntent): Flow<GroupEditorState> {
        return when (intent) {
            GroupEditorIntent.Initialize -> loadData()
            GroupEditorIntent.OnBackClick -> nonStateAction { navigateBack() }
            GroupEditorIntent.OnAddMemberClick -> onAddMemberClicked()
            GroupEditorIntent.OnDoneClick -> onDoneClicked()
            is GroupEditorIntent.OnTitleChanged -> onTitleChanged(intent)
            is GroupEditorIntent.OnPasswordChanged -> onPasswordChanged(intent)
            is GroupEditorIntent.OnConfirmPasswordChanged -> onConfirmPasswordChanged(intent)
            is GroupEditorIntent.OnMemberChanged -> onMemberChanged(intent)
            is GroupEditorIntent.OnRemoveMemberClick -> onRemoveMemberClicked(intent)
            is GroupEditorIntent.OnPasswordToggleClick -> onPasswordToggleClicked(intent)
            is GroupEditorIntent.OnConfirmPasswordToggleClick ->
                onConfirmPasswordToggleClicked(intent)
        }
    }

    private fun onTitleChanged(intent: GroupEditorIntent.OnTitleChanged): Flow<GroupEditorState> {
        dataState = dataState.copy(
            title = intent.title,
            titleError = null
        )

        return singleFlowOf(dataState)
    }

    private fun onPasswordChanged(
        intent: GroupEditorIntent.OnPasswordChanged
    ): Flow<GroupEditorState> {
        dataState = dataState.copy(
            password = intent.password,
            passwordError = null,
            confirmPasswordError = null
        )

        return singleFlowOf(dataState)
    }

    private fun onConfirmPasswordChanged(
        intent: GroupEditorIntent.OnConfirmPasswordChanged
    ): Flow<GroupEditorState> {
        dataState = dataState.copy(
            confirmPassword = intent.confirmPassword,
            confirmPasswordError = null
        )

        return singleFlowOf(dataState)
    }

    private fun onMemberChanged(intent: GroupEditorIntent.OnMemberChanged): Flow<GroupEditorState> {
        dataState = dataState.copy(member = intent.member)
        return singleFlowOf(dataState)
    }

    private fun onAddMemberClicked(): Flow<GroupEditorState> {
        val newMemberName = dataState.member.trim()
        if (newMemberName.isBlank()) {
            return emptyFlow()
        }

        if (newMemberName in dataState.members) {
            return emptyFlow()
        }

        dataState = dataState.copy(
            member = StringUtils.EMPTY,
            memberError = null,
            members = dataState.members + newMemberName
        )

        return flowOf(dataState)
    }

    private fun onRemoveMemberClicked(
        intent: GroupEditorIntent.OnRemoveMemberClick
    ): Flow<GroupEditorState> {
        val newMembers = dataState.members.toMutableList()
            .apply {
                removeAt(intent.memberIndex)
            }

        dataState = dataState.copy(members = newMembers)

        return flowOf(dataState)
    }

    private fun navigateBack() {
        router.exit()
    }

    private fun onPasswordToggleClicked(
        intent: GroupEditorIntent.OnPasswordToggleClick
    ): Flow<GroupEditorState> {
        dataState = dataState.copy(
            isPasswordVisible = intent.isVisible
        )

        return flowOf(dataState)
    }

    private fun onConfirmPasswordToggleClicked(
        intent: GroupEditorIntent.OnConfirmPasswordToggleClick
    ): Flow<GroupEditorState> {
        dataState = dataState.copy(
            isConfirmPasswordVisible = intent.isVisible
        )

        return flowOf(dataState)
    }

    private fun loadData(): Flow<GroupEditorState> {
        if (args.mode is GroupEditorMode.NewGroup) {
            return flowOf(GroupEditorState.Data())
        }

        return flow {
        }
    }

    private fun onDoneClicked(): Flow<GroupEditorState> {
        if (dataState.title.isBlank()) {
            dataState = dataState.copy(
                titleError = resourceProvider.getString(R.string.enter_group_name)
            )
            return flowOf(dataState)
        }

        if (dataState.password.isBlank()) {
            dataState = dataState.copy(
                passwordError = resourceProvider.getString(R.string.enter_password)
            )
            return flowOf(dataState)
        }

        if (dataState.confirmPassword.isBlank()) {
            dataState = dataState.copy(
                confirmPasswordError = resourceProvider.getString(R.string.enter_password)
            )
            return flowOf(dataState)
        }

        if (dataState.password.trim() != dataState.confirmPassword.trim()) {
            dataState = dataState.copy(
                confirmPasswordError = resourceProvider.getString(R.string.passwords_dont_match)
            )
            return flowOf(dataState)
        }

        if (dataState.members.size < 2) {
            dataState = dataState.copy(
                memberError = resourceProvider.getString(R.string.invalid_member_count_message)
            )
            return flowOf(dataState)
        }

        return flow {
            emit(GroupEditorState.Loading)

            val createGroupResult = interactor.createGroup(
                password = dataState.password.trim(),
                title = dataState.title.trim(),
                description = StringUtils.EMPTY,
                members = dataState.members
            )
            if (createGroupResult.isLeft()) {
                val message = createGroupResult.getErrorMessage()
                emit(GroupEditorState.Error(message))
                return@flow
            }

            val group = createGroupResult.getOrNull() ?: return@flow
            Timber.d("Successfully created group: uid=${group.group.uid}")

            router.exit()
        }
            .flowOn(Dispatchers.IO)
    }
}