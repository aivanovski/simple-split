package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.GroupDetailsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsIntent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GroupDetailsViewModel(
    private val interactor: GroupDetailsInteractor,
    private val cellFactory: GroupDetailsCellFactory,
    private val router: Router,
    private val args: GroupDetailsArgs
) : CellsMviViewModel<GroupDetailsState, GroupDetailsIntent>(
    initialState = GroupDetailsState.Loading,
    initialIntent = GroupDetailsIntent.Initialize
) {

    override fun handleIntent(intent: GroupDetailsIntent): Flow<GroupDetailsState> {
        return when (intent) {
            is GroupDetailsIntent.Initialize -> showData(args.group)
            is GroupDetailsIntent.OnBackClick -> navigateBack()
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        // TODO: implement
    }

    private fun navigateBack(): Flow<GroupDetailsState> {
        router.exit()
        return emptyFlow()
    }

    private fun loadData(groupUid: String): Flow<GroupDetailsState> {
        return flow {
            emit(GroupDetailsState.Loading)

            val getGroupDetailsResult = interactor.getGroup(
                groupUid = groupUid,
                password = args.password
            )

            if (getGroupDetailsResult.isLeft()) {
                val message = getGroupDetailsResult.getErrorMessage()
                emit(GroupDetailsState.Error(message))
                return@flow
            }

            val group = getGroupDetailsResult.getOrNull() ?: return@flow

            val viewModels = cellFactory.createCells(
                group = group,
                eventProvider = cellEventProvider
            )

            emit(GroupDetailsState.Data(viewModels))
        }
            .flowOn(Dispatchers.IO)
    }

    private fun showData(group: GroupDto): Flow<GroupDetailsState> {
        return flow<GroupDetailsState> {
            val viewModels = cellFactory.createCells(
                group = group,
                eventProvider = cellEventProvider
            )

            emit(GroupDetailsState.Data(viewModels))
        }
            .flowOn(Dispatchers.IO)
    }
}