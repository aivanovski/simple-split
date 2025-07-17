package com.github.ai.simplesplit.android.presentation.screens.groups

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.CellFactory
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GroupsViewModel(
    private val interactor: GroupsInteractor,
    private val router: Router
) : CellsMviViewModel<GroupsState, GroupsIntent>(
    initialState = GroupsState.Loading,
    initialIntent = GroupsIntent.Initialize
) {

    private val cellFactory = CellFactory()
    private var groups by mutableStateFlow(emptyList<GroupDto>())

    override fun handleIntent(intent: GroupsIntent): Flow<GroupsState> {
        return when (intent) {
            GroupsIntent.Initialize -> loadData()
            is GroupsIntent.OnGroupClick -> navigateToGroupDetails(intent.groupUid)
            is GroupsIntent.OnAddGroupClick -> navigateToGroupEditor()
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

            val getGroupsResult = interactor.getStoredGroups()
            if (getGroupsResult.isLeft()) {
                val message = getGroupsResult.getErrorMessage()
                emit(GroupsState.Error(message))
                return@flow
            }

            val groups = getGroupsResult.getOrNull() ?: emptyList()
            this@GroupsViewModel.groups = groups

            if (groups.isNotEmpty()) {
                val viewModels = cellFactory.createCells(
                    groups = groups,
                    eventProvider = cellEventProvider
                )
                emit(GroupsState.Data(viewModels))
            } else {
                emit(GroupsState.Empty)
            }
        }
            .flowOn(Dispatchers.IO)
    }

    private fun navigateToGroupDetails(groupUid: String): Flow<GroupsState> {
        val group = groups.firstOrNull { group -> group.uid == groupUid }
            ?: return emptyFlow()

        router.navigateTo(
            Screen.GroupDetails(
                GroupDetailsArgs(
                    group = group,
                    password = "abc123"
                )
            )
        )

        return emptyFlow()
    }

    private fun navigateToGroupEditor(): Flow<GroupsState> {
        router.navigateTo(
            Screen.GroupEditor(
                GroupEditorArgs(
                    mode = GroupEditorMode.NewGroup
                )
            )
        )

        return emptyFlow()
    }
}