package com.github.ai.simplesplit.android.presentation.screens.groups

import androidx.annotation.StringRes
import androidx.lifecycle.viewModelScope
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuItem
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.CellFactory
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsData
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsState
import com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
import com.github.ai.simplesplit.android.utils.StringUtils
import com.github.ai.simplesplit.android.utils.getStringOrNull
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.parseCellId
import com.github.ai.simplesplit.android.utils.toErrorMessage
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val interactor: GroupsInteractor,
    private val resources: ResourceProvider,
    private val router: Router
) : CellsMviViewModel<GroupsState, GroupsIntent>(
    initialState = GroupsState.Loading,
    initialIntent = GroupsIntent.Initialize
) {

    private val cellFactory = CellFactory()
    private var screenData by mutableStateFlow<GroupsData?>(null)

    init {
        doWhenStarted {
            viewModelScope.launch {
                interactor.getGroupCredentialsFlow()
                    .collect { _ ->
                        sendIntent(GroupsIntent.ReloadData)
                    }
            }
        }
    }

    override fun start() {
        super.start()
        sendIntent(GroupsIntent.ReloadDataInBackground)
    }

    override fun handleIntent(intent: GroupsIntent): Flow<GroupsState> {
        return when (intent) {
            GroupsIntent.Initialize -> loadData()
            GroupsIntent.ReloadData -> loadData()
            GroupsIntent.ReloadDataInBackground -> loadData(isShowLoading = false)

            is GroupsIntent.OnGroupClick ->
                nonStateAction { navigateToGroupDetails(intent.groupUid) }

            is GroupsIntent.OnGroupLongClick ->
                nonStateAction { showGroupMenuDialog(intent.groupUid) }

            is GroupsIntent.OnAddButtonClick ->
                nonStateAction { showAddGroupMenuDialog() }

            is GroupsIntent.OnEditGroupClick ->
                nonStateAction { navigateToGroupEditor(intent.groupUid) }

            is GroupsIntent.OnRemoveGroupClick ->
                nonStateAction { showRemoveConfirmationDialog(intent.groupUid) }

            is GroupsIntent.OnRemoveGroupConfirmed -> removeGroup(intent.groupUid)

            is GroupsIntent.OnCloseErrorClick -> onCloseErrorClicked()

            is GroupsIntent.OnErrorActionClick -> handleErrorAction(intent.actionId)

            is GroupsIntent.OnCreateGroupClick ->
                nonStateAction { navigateToNewGroupScreen() }

            is GroupsIntent.OnAddGroupByUrlClick ->
                nonStateAction { navigateToCheckoutGroupScreen() }

            is GroupsIntent.OpenUrl ->
                nonStateAction { router.startActivity(StartActivityEvent.OpenUrl(intent.url)) }

            is GroupsIntent.ShareUrl ->
                nonStateAction { router.startActivity(StartActivityEvent.ShareUrl(intent.url)) }

            GroupsIntent.OnSettingsClick ->
                nonStateAction { navigateToSettingsScreen() }
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is GroupCellEvent.OnClick -> {
                val groupUid = getGroupUidFromCellId(event.cellId) ?: return
                sendIntent(GroupsIntent.OnGroupClick(groupUid = groupUid))
            }

            is GroupCellEvent.OnLongClick -> {
                val groupUid = getGroupUidFromCellId(event.cellId) ?: return
                sendIntent(GroupsIntent.OnGroupLongClick(groupUid = groupUid))
            }
        }
    }

    private fun loadData(isShowLoading: Boolean = true): Flow<GroupsState> {
        return flow {
            if (isShowLoading) {
                emit(GroupsState.Loading)
            }

            interactor.loadData()
                .fold(
                    ifLeft = {
                        emit(GroupsState.Error(it.toErrorMessage(resources)))
                    },
                    ifRight = { data ->
                        emit(createScreenState(data))
                    }
                )
        }.flowOn(Dispatchers.IO)
    }

    private fun createScreenState(data: GroupsData): GroupsState {
        screenData = data

        val missingGroupsError = if (data.groups.size != data.requestedCredentials.size) {
            ErrorMessage(
                message = resources.getString(R.string.missing_groups_error_message),
                actionText = resources.getString(R.string.forget_missing),
                actionId = ErrorAction.FORGET_MISSING_GROUPS.ordinal
            )
        } else {
            null
        }

        return if (data.groups.isNotEmpty()) {
            val viewModels = cellFactory.createCells(
                data = data,
                eventProvider = cellEventProvider
            )
            GroupsState.Data(
                cellViewModels = viewModels,
                error = missingGroupsError
            )
        } else {
            GroupsState.Empty(error = missingGroupsError)
        }
    }

    private fun removeGroup(groupUid: String): Flow<GroupsState> {
        return flow {
            emit(GroupsState.Loading)

            interactor.removeGroup(groupUid)

            emitAll(loadData())
        }.flowOn(Dispatchers.IO)
    }

    private fun navigateToGroupDetails(groupUid: String) {
        val groups = screenData?.groups ?: emptyList()
        val credentials = screenData?.requestedCredentials ?: emptyList()

        val groupAndCreds = groups.zip(credentials)
            .firstOrNull { (group, _) -> group.uid == groupUid } ?: return

        router.navigateTo(
            Screen.GroupDetails(
                GroupDetailsArgs(
                    group = groupAndCreds.first,
                    password = groupAndCreds.second.password
                )
            )
        )
    }

    private fun navigateToGroupEditor(groupUid: String) {
        val creds = getGroupCredentials(groupUid) ?: return

        router.navigateTo(
            Screen.GroupEditor(
                GroupEditorArgs(
                    mode = GroupEditorMode.EditGroup(creds)
                )
            )
        )
    }

    private fun navigateToNewGroupScreen() {
        router.navigateTo(
            Screen.GroupEditor(
                GroupEditorArgs(
                    mode = GroupEditorMode.NewGroup
                )
            )
        )
    }

    private fun navigateToCheckoutGroupScreen() {
        router.navigateTo(
            Screen.CheckoutGroup(
                CheckoutGroupArgs(
                    url = StringUtils.EMPTY
                )
            )
        )
    }

    private fun navigateToSettingsScreen() {
        router.navigateTo(Screen.Settings)
    }

    private fun showAddGroupMenuDialog() {
        router.showDialog(
            Dialog.MenuDialog(
                MenuDialogArgs(
                    items = listOf(
                        MenuItem(
                            icon = AppIcon.ADD,
                            text = resources.getString(R.string.create_new_group),
                            actionId = MenuActions.CREATE_GROUP
                        ),
                        MenuItem(
                            icon = AppIcon.LINK,
                            text = resources.getString(R.string.add_by_url),
                            actionId = MenuActions.ADD_GROUP_BY_URL
                        )
                    )
                )
            )
        )
        router.setResultListener(Dialog.MenuDialog::class) { item ->
            if (item is MenuItem) {
                onAddMenuItemClicked(actionId = item.actionId)
            }
        }
    }

    private fun showGroupMenuDialog(groupUid: String) {
        val items = GroupMenuAction.entries.map { entry ->
            MenuItem(
                icon = entry.icon,
                text = resources.getString(entry.resourceId),
                actionId = entry.ordinal
            )
        }

        router.showDialog(Dialog.MenuDialog(MenuDialogArgs(items)))
        router.setResultListener(Dialog.MenuDialog::class) { item ->
            if (item is MenuItem) {
                val action = GroupMenuAction.entries.first { action ->
                    action.ordinal == item.actionId
                }

                onGroupMenuItemClicked(
                    action = action,
                    groupUid = groupUid
                )
            }
        }
    }

    private fun showRemoveConfirmationDialog(groupUid: String) {
        router.showDialog(
            Dialog.ConfirmationDialog(
                args = ConfirmationDialogArgs(
                    message = resources.getString(
                        R.string.remove_group_confirmation_message
                    ),
                    buttonTitle = resources.getString(R.string.remove)
                )
            )
        )
        router.setResultListener(Dialog.ConfirmationDialog::class) { isConfirmed ->
            if (isConfirmed is Boolean && isConfirmed == true) {
                sendIntent(GroupsIntent.OnRemoveGroupConfirmed(groupUid))
            }
        }
    }

    private fun onGroupMenuItemClicked(
        action: GroupMenuAction,
        groupUid: String
    ) {
        when (action) {
            GroupMenuAction.EDIT_GROUP -> sendIntent(GroupsIntent.OnEditGroupClick(groupUid))

            GroupMenuAction.REMOVE_GROUP -> sendIntent(GroupsIntent.OnRemoveGroupClick(groupUid))

            GroupMenuAction.EXPORT_TO_CSV -> {
                val creds = getGroupCredentials(groupUid) ?: return
                val url = interactor.createExportToCsvUrl(creds)
                sendIntent(GroupsIntent.OpenUrl(url))
            }

            GroupMenuAction.SHARE_LINK -> {
                val creds = getGroupCredentials(groupUid) ?: return
                val url = interactor.createShareUrl(creds)
                sendIntent(GroupsIntent.ShareUrl(url))
            }
        }
    }

    private fun onAddMenuItemClicked(actionId: Int) {
        when (actionId) {
            MenuActions.CREATE_GROUP -> sendIntent(GroupsIntent.OnCreateGroupClick)
            MenuActions.ADD_GROUP_BY_URL -> sendIntent(GroupsIntent.OnAddGroupByUrlClick)
            else -> throw IllegalArgumentException("Illegal actionId: $actionId")
        }
    }

    private fun onCloseErrorClicked(): Flow<GroupsState> {
        val state = this.state.value

        return flowOf(
            when (state) {
                is GroupsState.Empty -> state.copy(error = null)
                is GroupsState.Data -> state.copy(error = null)
                else -> state
            }
        )
    }

    private fun handleErrorAction(actionId: Int): Flow<GroupsState> {
        val action = ErrorAction.entries.getOrNull(actionId) ?: return emptyFlow()

        return when (action) {
            ErrorAction.FORGET_MISSING_GROUPS -> onForgetMissingGroupsClicked()
        }
    }

    private fun onForgetMissingGroupsClicked(): Flow<GroupsState> {
        val data = screenData ?: return emptyFlow()

        return flow {
            emit(GroupsState.Loading)

            val foundGroupsUids = data.groups
                .map { group -> group.uid }
                .toSet()

            val missingGroupsUids = data.requestedCredentials
                .filter { credentials -> credentials.groupUid !in foundGroupsUids }
                .map { credentials -> credentials.groupUid }

            val removeResult = interactor.removeGroups(missingGroupsUids)
            if (removeResult.isLeft()) {
                val message = removeResult.toErrorMessage(resources)
                emit(GroupsState.Error(message))
                return@flow
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun getGroupUidFromCellId(cellId: String): String? {
        return cellId.parseCellId()?.payload?.getStringOrNull()
    }

    private fun getGroupCredentials(groupUid: String): GroupCredentials? {
        return screenData?.requestedCredentials
            ?.firstOrNull { creds -> creds.groupUid == groupUid }
    }

    enum class GroupMenuAction(
        val icon: AppIcon,
        @StringRes val resourceId: Int
    ) {
        EDIT_GROUP(AppIcon.EDIT, R.string.edit),
        REMOVE_GROUP(AppIcon.REMOVE, R.string.remove),
        EXPORT_TO_CSV(AppIcon.EXPORT, R.string.export_as_csv_file),
        SHARE_LINK(AppIcon.SHARE, R.string.share)
    }

    enum class ErrorAction {
        FORGET_MISSING_GROUPS
    }

    object MenuActions {
        const val CREATE_GROUP = 102
        const val ADD_GROUP_BY_URL = 103
    }
}