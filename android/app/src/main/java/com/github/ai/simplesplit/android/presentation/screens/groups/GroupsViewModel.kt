package com.github.ai.simplesplit.android.presentation.screens.groups

import androidx.lifecycle.viewModelScope
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
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
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.launch

class GroupsViewModel(
    private val interactor: GroupsInteractor,
    private val router: Router
) : CellsMviViewModel<GroupsState, GroupsIntent>(
    initialState = GroupsState.Loading,
    initialIntent = GroupsIntent.Initialize
) {

    private val cellFactory = CellFactory()
    private var screenData by mutableStateFlow(GroupsData())

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

    override fun handleIntent(intent: GroupsIntent): Flow<GroupsState> {
        return when (intent) {
            GroupsIntent.Initialize -> loadData()
            GroupsIntent.ReloadData -> loadData()

            is GroupsIntent.OnGroupClick ->
                nonStateAction { navigateToGroupDetails(intent.groupUid) }

            is GroupsIntent.OnAddGroupClick ->
                nonStateAction { navigateToGroupEditor() }
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is GroupCellEvent.OnClick -> {
                sendIntent(GroupsIntent.OnGroupClick(groupUid = event.cellId))
            }
        }
    }

    private fun loadData(): Flow<GroupsState> {
        return flow {
            emit(GroupsState.Loading)

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
        }
            .flowOn(Dispatchers.IO)
    }

    private fun navigateToGroupDetails(
        groupUid: String
    ) {
        val groupAndCreds = screenData.groups
            .zip(screenData.credentials)
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

    private fun navigateToGroupEditor() {
        router.navigateTo(
            Screen.GroupEditor(
                GroupEditorArgs(
                    mode = GroupEditorMode.NewGroup
                )
            )
        )
    }
}