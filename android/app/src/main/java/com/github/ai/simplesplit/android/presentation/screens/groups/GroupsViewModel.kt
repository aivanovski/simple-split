package com.github.ai.simplesplit.android.presentation.screens.groups

import androidx.lifecycle.viewModelScope
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.Icon
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuItem
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.CellFactory
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsData
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.getStringOrNull
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.parseCellId
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val interactor: GroupsInteractor,
    private val resourceProvider: ResourceProvider,
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

            is GroupsIntent.OnAddGroupClick ->
                nonStateAction { navigateToNewGroupScreen() }

            is GroupsIntent.OnEditGroupClick ->
                nonStateAction { navigateToGroupEditor(intent.groupUid) }

            is GroupsIntent.OnRemoveGroupClick ->
                nonStateAction { showRemoveConfirmationDialog(intent.groupUid) }

            is GroupsIntent.OnRemoveGroupConfirmed -> removeGroup(intent.groupUid)
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

            val getDataResult = interactor.loadData()
            if (getDataResult.isLeft()) {
                val message = getDataResult.getErrorMessage()
                emit(GroupsState.Error(message))
                return@flow
            }

            val data = getDataResult.getOrNull() ?: GroupsData()
            screenData = data

            if (data.groups.isNotEmpty()) {
                val viewModels = cellFactory.createCells(
                    groups = data.groups,
                    eventProvider = cellEventProvider
                )
                emit(GroupsState.Data(viewModels))
            } else {
                emit(GroupsState.Empty)
            }
        }.flowOn(Dispatchers.IO)
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
        val credentials = screenData?.credentials ?: emptyList()

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
        val creds = screenData?.credentials
            ?.firstOrNull { creds -> creds.groupUid == groupUid }
            ?: return

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

    private fun showGroupMenuDialog(groupUid: String) {
        router.showDialog(
            Dialog.MenuDialog(
                MenuDialogArgs(
                    items = listOf(
                        MenuItem(
                            icon = Icon.EDIT,
                            text = resourceProvider.getString(R.string.edit),
                            actionId = MenuActions.EDIT_GROUP
                        ),
                        MenuItem(
                            icon = Icon.REMOVE,
                            text = resourceProvider.getString(R.string.remove),
                            actionId = MenuActions.REMOVE_GROUP
                        )
                    )
                )
            )
        )
        router.setResultListener(Dialog.MenuDialog::class) { item ->
            if (item is MenuItem) {
                onMenuItemClicked(
                    actionId = item.actionId,
                    groupUid = groupUid
                )
            }
        }
    }

    private fun showRemoveConfirmationDialog(groupUid: String) {
        router.showDialog(
            Dialog.ConfirmationDialog(
                args = ConfirmationDialogArgs(
                    message = resourceProvider.getString(
                        R.string.remove_group_confirmation_message
                    ),
                    buttonTitle = resourceProvider.getString(R.string.remove)
                )
            )
        )
        router.setResultListener(Dialog.ConfirmationDialog::class) { isConfirmed ->
            if (isConfirmed is Boolean && isConfirmed == true) {
                sendIntent(GroupsIntent.OnRemoveGroupConfirmed(groupUid))
            }
        }
    }

    private fun onMenuItemClicked(
        actionId: Int,
        groupUid: String
    ) {
        when (actionId) {
            MenuActions.EDIT_GROUP ->
                sendIntent(GroupsIntent.OnEditGroupClick(groupUid = groupUid))

            MenuActions.REMOVE_GROUP -> {
                sendIntent(GroupsIntent.OnRemoveGroupClick(groupUid = groupUid))
            }
        }
    }

    private fun getGroupUidFromCellId(cellId: String): String? {
        return cellId.parseCellId()?.payload?.getStringOrNull()
    }

    object MenuActions {

        const val EDIT_GROUP = 100
        const val REMOVE_GROUP = 101
    }
}