package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.GroupDetailsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsIntent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
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
            GroupDetailsIntent.Initialize -> loadData(args.group.uid, args.password)
            GroupDetailsIntent.ReloadData -> loadData(args.group.uid, args.password)
            GroupDetailsIntent.OnBackClick -> nonStateAction { navigateBack() }
            GroupDetailsIntent.OnFabClick ->
                nonStateAction { navigateToNewExpenseScreen() }
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        // TODO: implement
    }

    private fun navigateToNewExpenseScreen() {
        router.navigateTo(
            Screen.ExpenseEditor(
                ExpenseEditorArgs(
                    mode = ExpenseEditorMode.NewExpense(
                        group = args.group
                    )
                )
            )
        )
        router.setResultListener(Screen.ExpenseEditor::class) { _ ->
            sendIntent(GroupDetailsIntent.ReloadData)
        }
    }

    private fun navigateBack() {
        router.exit()
    }

    private fun loadData(
        groupUid: String,
        password: String
    ): Flow<GroupDetailsState> {
        return flow {
            emit(GroupDetailsState.Loading)

            val getGroupDetailsResult = interactor.getGroup(
                groupUid = groupUid,
                password = password
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