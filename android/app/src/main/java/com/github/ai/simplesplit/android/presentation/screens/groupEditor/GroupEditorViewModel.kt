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
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.ValidationResult
import com.github.ai.simplesplit.android.utils.StringUtils
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.singleFlowOf
import com.github.ai.simplesplit.android.utils.toErrorMessage
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class GroupEditorViewModel(
    private val interactor: GroupEditorInteractor,
    private val resources: ResourceProvider,
    private val router: Router,
    private val args: GroupEditorArgs
) : MviViewModel<GroupEditorState, GroupEditorIntent>(
    initialState = GroupEditorState.Loading,
    initialIntent = GroupEditorIntent.Initialize
) {

    private var dataState by mutableStateFlow(GroupEditorState.Data())
    private var data by mutableStateFlow<GroupDto?>(null)

    override fun handleIntent(intent: GroupEditorIntent): Flow<GroupEditorState> {
        return when (intent) {
            GroupEditorIntent.Initialize -> loadData()
            GroupEditorIntent.OnBackClick -> nonStateAction { navigateBack() }
            GroupEditorIntent.OnAddMemberClick -> onAddMemberClicked()
            GroupEditorIntent.OnDoneClick -> onDoneClicked()
            GroupEditorIntent.OnCloseErrorClick -> onCloseErrorClicked()
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

    private fun onDoneClicked(): Flow<GroupEditorState> {
        val validationResult = when (args.mode) {
            is GroupEditorMode.NewGroup -> validateNewGroupData(dataState)
            is GroupEditorMode.EditGroup -> validateEditGroupData(dataState)
        }

        if (validationResult is ValidationResult.Invalid) {
            dataState = dataState.copy(
                titleError = validationResult.titleError,
                passwordError = validationResult.passwordError,
                confirmPasswordError = validationResult.confirmationError,
                memberError = validationResult.membersError
            )
            return flowOf(dataState)
        }

        return flow {
            emit(GroupEditorState.Loading)

            val response = when (args.mode) {
                is GroupEditorMode.NewGroup -> {
                    interactor.createGroup(
                        password = dataState.password.trim(),
                        title = dataState.title.trim(),
                        members = dataState.members
                    )
                }

                is GroupEditorMode.EditGroup -> {
                    val membersToAdd = getNewMembers()
                    val memberToDelete = getMemberUidsToDelete()

                    interactor.updateGroup(
                        credentials = args.mode.credentials,
                        newTitle = dataState.title.ifBlank { null },
                        newPassword = dataState.password.ifBlank { null },
                        membersToRemove = memberToDelete,
                        membersToAdd = membersToAdd
                    )
                }
            }

            response.fold(
                ifLeft = { error ->
                    dataState = dataState.copy(
                        error = error.toErrorMessage(resources)
                    )
                    emit(dataState)
                },
                ifRight = {
                    router.exit()
                }
            )
        }
            .flowOn(Dispatchers.IO)
    }

    private fun loadData(): Flow<GroupEditorState> {
        return when (args.mode) {
            is GroupEditorMode.NewGroup -> flowOf(GroupEditorState.Data())
            is GroupEditorMode.EditGroup -> flow {
                emit(GroupEditorState.Loading)

                interactor.loadGroup(
                    uid = args.mode.credentials.groupUid,
                    password = args.mode.credentials.password
                ).fold(
                    ifLeft = { error ->
                        emit(GroupEditorState.Error(error.toErrorMessage(resources)))
                    },
                    ifRight = { group ->
                        data = group
                        dataState = GroupEditorState.Data(
                            title = group.title,
                            members = group.members.map { member -> member.name }
                        )
                        emit(dataState)
                    }
                )
            }.flowOn(Dispatchers.IO)
        }
    }

    private fun getNewMembers(): List<String> {
        val memberNames = data?.members
            ?.map { member -> member.name }
            ?.toSet()
            ?: emptySet()

        val newMemberNames = dataState.members
            .filter { memberName -> memberName !in memberNames }

        return newMemberNames
    }

    private fun getMemberUidsToDelete(): List<String> {
        val members = data?.members ?: return emptyList()

        val memberNameToUidMap = members.associateBy { member -> member.name }
        val removedNames = memberNameToUidMap.keys - dataState.members.toSet()

        return removedNames.mapNotNull { name ->
            memberNameToUidMap[name]?.uid
        }
    }

    private fun onCloseErrorClicked(): Flow<GroupEditorState> {
        dataState = dataState.copy(
            error = null
        )
        return flowOf(dataState)
    }

    private fun validateEditGroupData(state: GroupEditorState.Data): ValidationResult {
        val title = state.title
        val password = state.password
        val confirmPassword = state.confirmPassword
        val members = state.members

        val emptyTitleError = if (title.isBlank()) {
            resources.getString(R.string.enter_group_name)
        } else {
            null
        }

        val membersError = if (members.size < 2) {
            resources.getString(R.string.invalid_member_count_message)
        } else {
            null
        }

        val (passwordError, confirmationError) = if (password.isNotBlank() ||
            confirmPassword.isNotBlank()
        ) {
            val emptyPasswordError = if (password.isBlank()) {
                resources.getString(R.string.enter_password)
            } else {
                null
            }

            val emptyConfirmationError = if (confirmPassword.isBlank()) {
                resources.getString(R.string.enter_password)
            } else {
                null
            }

            val passwordConfirmationError = if (password.trim() != confirmPassword.trim()) {
                resources.getString(R.string.passwords_dont_match)
            } else {
                null
            }

            emptyPasswordError to (emptyConfirmationError ?: passwordConfirmationError)
        } else {
            null to null
        }

        val errors = listOfNotNull(
            emptyTitleError,
            passwordError,
            confirmationError,
            membersError
        )

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                titleError = emptyTitleError,
                passwordError = passwordError,
                confirmationError = confirmationError,
                membersError = membersError
            )
        }
    }

    private fun validateNewGroupData(state: GroupEditorState.Data): ValidationResult {
        val title = state.title
        val password = state.password
        val confirmPassword = state.confirmPassword
        val members = state.members

        val emptyTitleError = if (title.isBlank()) {
            resources.getString(R.string.enter_group_name)
        } else {
            null
        }

        val emptyPasswordError = if (title.isBlank()) {
            resources.getString(R.string.enter_password)
        } else {
            null
        }

        val emptyConfirmationError = if (confirmPassword.isBlank()) {
            resources.getString(R.string.enter_password)
        } else {
            null
        }

        val passwordConfirmationError = if (password.trim() != confirmPassword.trim()) {
            resources.getString(R.string.passwords_dont_match)
        } else {
            null
        }

        val membersError = if (members.size < 2) {
            resources.getString(R.string.invalid_member_count_message)
        } else {
            null
        }

        val errors = listOfNotNull(
            emptyTitleError,
            emptyPasswordError,
            emptyConfirmationError,
            passwordConfirmationError,
            membersError
        )

        return if (errors.isEmpty()) {
            ValidationResult.Valid
        } else {
            ValidationResult.Invalid(
                titleError = emptyTitleError,
                passwordError = emptyPasswordError,
                confirmationError = emptyConfirmationError ?: passwordConfirmationError,
                membersError = membersError
            )
        }
    }
}