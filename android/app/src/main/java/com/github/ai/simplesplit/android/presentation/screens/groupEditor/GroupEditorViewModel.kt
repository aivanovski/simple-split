package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import arrow.core.Either
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.model.exception.AppException
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.MviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorData
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorState
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.MemberItem
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
    private var currentGroup by mutableStateFlow<GroupDto?>(null)
    private var allCurrencies by mutableStateFlow<List<CurrencyEntity>?>(null)
    private var selectedCurrency by mutableStateFlow<CurrencyEntity?>(null)
    private var selectedMember by mutableStateFlow<MemberItem?>(null)

    override fun handleIntent(intent: GroupEditorIntent): Flow<GroupEditorState> {
        return when (intent) {
            GroupEditorIntent.Initialize -> loadData()
            GroupEditorIntent.OnBackClick -> nonStateAction { navigateBack() }
            GroupEditorIntent.OnAddMemberClick -> onAddMemberClicked()
            GroupEditorIntent.OnDoneClick -> onDoneClicked()
            GroupEditorIntent.OnCloseErrorClick -> onCloseErrorClicked()
            GroupEditorIntent.OnCurrencyClick ->
                nonStateAction { showSelectCurrencyDialog() }

            is GroupEditorIntent.OnTitleChanged -> onTitleChanged(intent)
            is GroupEditorIntent.OnPasswordChanged -> onPasswordChanged(intent)
            is GroupEditorIntent.OnConfirmPasswordChanged -> onConfirmPasswordChanged(intent)
            is GroupEditorIntent.OnMemberChanged -> onMemberChanged(intent)
            is GroupEditorIntent.OnRemoveMemberClick -> onRemoveMemberClicked(intent)
            is GroupEditorIntent.OnPasswordToggleClick -> onPasswordToggleClicked(intent)
            is GroupEditorIntent.OnConfirmPasswordToggleClick ->
                onConfirmPasswordToggleClicked(intent)

            GroupEditorIntent.OnCancelMemberEditClick -> onCancelMemberEditClicked()
            GroupEditorIntent.OnApplyMemberEditClick -> onApplyMemberEditClicked()
            is GroupEditorIntent.OnEditMemberClick -> onEditMemberClicked(intent.memberIndex)
            is GroupEditorIntent.OnCurrencySelected -> onCurrencySelected(intent.currency)
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

        val isMemberAlreadyAdded = dataState.members
            .any { member -> member.name == newMemberName }

        if (isMemberAlreadyAdded) {
            return emptyFlow()
        }

        dataState = dataState.copy(
            member = StringUtils.EMPTY,
            memberError = null,
            members = dataState.members + MemberItem(newMemberName)
        )

        return flowOf(dataState)
    }

    private fun onEditMemberClicked(memberIndex: Int): Flow<GroupEditorState> {
        val member = dataState.members.getOrNull(memberIndex) ?: return emptyFlow()
        selectedMember = member

        dataState = dataState.copy(
            member = member.name,
            memberError = null,
            isApplyButtonVisible = true,
            isAddButtonVisible = false,
            isCancelButtonVisible = true
        )

        return flowOf(dataState)
    }

    private fun onCancelMemberEditClicked(): Flow<GroupEditorState> {
        dataState = dataState.copy(
            member = StringUtils.EMPTY,
            memberError = null,
            isApplyButtonVisible = false,
            isAddButtonVisible = true,
            isCancelButtonVisible = false
        )

        return flowOf(dataState)
    }

    private fun onApplyMemberEditClicked(): Flow<GroupEditorState> {
        val selectedMember = selectedMember ?: return emptyFlow()

        val newMemberName = dataState.member.trim()

        val isNameAlreadyExist = dataState.members
            .filter { member -> member != selectedMember }
            .any { member -> member.name == newMemberName }

        if (isNameAlreadyExist) {
            dataState = dataState.copy(
                memberError = resources.getString(R.string.non_unique_member_name)
            )

            return flowOf(dataState)
        }

        val newMember = selectedMember.copy(name = newMemberName)

        val newMembers = dataState.members
            .filter { member -> member != selectedMember }
            .toMutableList()
            .apply {
                add(newMember)
            }

        dataState = dataState.copy(
            members = newMembers,
            member = StringUtils.EMPTY,
            memberError = null,
            isApplyButtonVisible = false,
            isAddButtonVisible = true,
            isCancelButtonVisible = false
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
                        currencyIsoCode = selectedCurrency?.isoCode ?: StringUtils.EMPTY,
                        members = dataState.members.map { member -> member.name }
                    )
                }

                is GroupEditorMode.EditGroup -> {
                    val membersToAdd = getMembersToAdd()
                    val membersToDelete = getMemberUidsToDelete()
                    val membersToUpdate = getMembersToUpdate()

                    interactor.updateGroup(
                        credentials = args.mode.credentials,
                        newTitle = dataState.title.ifBlank { null },
                        newPassword = dataState.password.ifBlank { null },
                        newCurrencyIsoCode = selectedCurrency?.isoCode?.ifBlank { null },
                        memberUidsToRemove = membersToDelete,
                        memberNamesToAdd = membersToAdd.map { it.name },
                        membersToUpdate = membersToUpdate.map { Pair(it.uid ?: "", it.name) }
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
            is GroupEditorMode.NewGroup -> flow {
                emit(GroupEditorState.Loading)

                val getCurrenciesResult = interactor.loadCurrencies()
                emit(onNewGroupDataLoaded(getCurrenciesResult))
            }

            is GroupEditorMode.EditGroup -> flow {
                emit(GroupEditorState.Loading)

                val loadDataResult = interactor.loadGroup(
                    uid = args.mode.credentials.groupUid,
                    password = args.mode.credentials.password
                )
                emit(onGroupDataLoaded(loadDataResult))
            }.flowOn(Dispatchers.IO)
        }
    }

    private fun onNewGroupDataLoaded(
        result: Either<AppException, List<CurrencyEntity>>
    ): GroupEditorState {
        return result.fold(
            ifLeft = { error ->
                GroupEditorState.Error(error.toErrorMessage(resources))
            },
            ifRight = { data ->
                val currency = determineDefaultCurrency(data)

                allCurrencies = data
                selectedCurrency = currency

                dataState = dataState.copy(
                    currency = formatCurrency(currency),
                    isAddButtonVisible = true
                )

                dataState
            }
        )
    }

    private fun onGroupDataLoaded(result: Either<AppException, GroupEditorData>): GroupEditorState {
        return result.fold(
            ifLeft = { error ->
                GroupEditorState.Error(error.toErrorMessage(resources))
            },
            ifRight = { data ->
                allCurrencies = data.currencies
                currentGroup = data.group

                val currency = data.currencies
                    .firstOrNull { currency -> currency.isoCode == data.group.currency.isoCode }
                    ?: determineDefaultCurrency(data.currencies)

                selectedCurrency = currency

                dataState = GroupEditorState.Data(
                    title = data.group.title,
                    currency = formatCurrency(currency),
                    isAddButtonVisible = true,
                    members = data.group.members.map { member ->
                        MemberItem(
                            name = member.name,
                            uid = member.uid
                        )
                    }
                )

                dataState
            }
        )
    }

    private fun getMembersToAdd(): List<MemberItem> {
        return dataState.members
            .filter { member -> member.uid == null }
    }

    private fun getMemberUidsToDelete(): List<String> {
        val currentMember = currentGroup?.members ?: return emptyList()

        val oldMemberUidToNameMap = currentMember.associateBy { member -> member.uid }

        val newMemberUids = dataState.members
            .mapNotNull { member -> member.uid }
            .toSet()

        val uidsToDelete = oldMemberUidToNameMap.keys
            .filter { uid -> uid !in newMemberUids }

        return uidsToDelete
    }

    private fun getMembersToUpdate(): List<MemberItem> {
        val oldMembers = currentGroup?.members ?: return emptyList()

        val oldMemberUidToNameMap = oldMembers
            .map { member -> member.uid to member.name }
            .toMap()

        return dataState.members
            .filter { member ->
                if (member.uid != null) {
                    val oldName = oldMemberUidToNameMap[member.uid]
                    oldName != member.name
                } else {
                    false
                }
            }
    }

    private fun onCloseErrorClicked(): Flow<GroupEditorState> {
        dataState = dataState.copy(
            error = null
        )
        return flowOf(dataState)
    }

    private fun showSelectCurrencyDialog() {
        router.showDialog(
            Dialog.SelectCurrency(
                SelectCurrencyDialogArgs(
                    selectedIsoCurrencyCode = selectedCurrency?.isoCode
                )
            )
        )
        router.setResultListener(Dialog.SelectCurrency::class) { currency ->
            if (currency is CurrencyEntity) {
                sendIntent(GroupEditorIntent.OnCurrencySelected(currency))
            }
        }
    }

    private fun onCurrencySelected(currency: CurrencyEntity): Flow<GroupEditorState> {
        selectedCurrency = currency

        dataState = dataState.copy(
            currency = formatCurrency(currency)
        )

        return flowOf(dataState)
    }

    private fun determineDefaultCurrency(currencies: List<CurrencyEntity>): CurrencyEntity {
        return currencies.first { currency ->
            currency.isoCode == "EUR"
        }
    }

    private fun formatCurrency(currency: CurrencyEntity): String {
        return if (currency.symbol.isNotBlank()) {
            currency.name + " - " + currency.symbol
        } else {
            currency.name
        }
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