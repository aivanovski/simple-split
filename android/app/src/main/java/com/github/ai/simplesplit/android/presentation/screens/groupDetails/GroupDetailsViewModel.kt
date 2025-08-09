package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import androidx.annotation.StringRes
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.model.db.GroupCredentials
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.Icon
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsAction
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuItem
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorMode
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.GroupDetailsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellEvent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsIntent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.getStringOrNull
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.parseCellId
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emitAll
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn

class GroupDetailsViewModel(
    private val interactor: GroupDetailsInteractor,
    private val cellFactory: GroupDetailsCellFactory,
    private val resourceProvider: ResourceProvider,
    private val router: Router,
    private val args: GroupDetailsArgs
) : CellsMviViewModel<GroupDetailsState, GroupDetailsIntent>(
    initialState = GroupDetailsState.Loading,
    initialIntent = GroupDetailsIntent.Initialize
) {

    private var data by mutableStateFlow(args.group)

    override fun handleIntent(intent: GroupDetailsIntent): Flow<GroupDetailsState> {
        return when (intent) {
            GroupDetailsIntent.Initialize -> loadData(args.group.uid, args.password)
            GroupDetailsIntent.ReloadData -> loadData(args.group.uid, args.password)
            GroupDetailsIntent.OnBackClick -> nonStateAction { navigateBack() }
            GroupDetailsIntent.OnFabClick ->
                nonStateAction { navigateToNewExpenseScreen() }

            is GroupDetailsIntent.OnExpenseClick ->
                nonStateAction { showExpenseDetailsDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnExpenseLongClick ->
                nonStateAction { showExpenseMenuDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnEditExpenseClick ->
                nonStateAction { navigateToEditExpenseScreen(intent.expenseUid) }

            is GroupDetailsIntent.OnRemoveExpenseClick ->
                nonStateAction { showRemoveExpenseConfirmationDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnRemoveExpenseConfirmed -> removeExpense(intent.expenseUid)
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is ExpenseCellEvent.OnClick -> {
                val expenseUid = getExpenseUidFromCellId(event.cellId) ?: return
                sendIntent(GroupDetailsIntent.OnExpenseClick(expenseUid))
            }

            is ExpenseCellEvent.OnLongClick -> {
                val expenseUid = getExpenseUidFromCellId(event.cellId) ?: return
                sendIntent(GroupDetailsIntent.OnExpenseLongClick(expenseUid))
            }
        }
    }

    private fun navigateToNewExpenseScreen() {
        router.navigateTo(
            Screen.ExpenseEditor(
                ExpenseEditorArgs(
                    mode = ExpenseEditorMode.NewExpense,
                    group = data,
                    credentials = GroupCredentials(args.group.uid, args.password)
                )
            )
        )
        router.setResultListener(Screen.ExpenseEditor::class) { _ ->
            sendIntent(GroupDetailsIntent.ReloadData)
        }
    }

    private fun navigateToEditExpenseScreen(expenseUid: String) {
        router.navigateTo(
            Screen.ExpenseEditor(
                ExpenseEditorArgs(
                    mode = ExpenseEditorMode.EditExpense(
                        expenseUid = expenseUid
                    ),
                    group = data,
                    credentials = GroupCredentials(args.group.uid, args.password)
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

            val getGroupResult = interactor.getGroup(
                groupUid = groupUid,
                password = password
            )
            if (getGroupResult.isLeft()) {
                emit(GroupDetailsState.Error(getGroupResult.getErrorMessage()))
                return@flow
            }

            val groupDto = getGroupResult.getOrNull() ?: return@flow
            data = groupDto

            emitAll(showData(groupDto))
        }.flowOn(Dispatchers.IO)
    }

    private fun removeExpense(expenseUid: String): Flow<GroupDetailsState> {
        return flow {
            emit(GroupDetailsState.Loading)

            val response = interactor.removeExpense(
                password = args.password,
                expenseUid = expenseUid
            )
            if (response.isLeft()) {
                emit(GroupDetailsState.Error(response.getErrorMessage()))
                return@flow
            }

            val group = response.getOrNull() ?: return@flow

            emitAll(showData(group))
        }.flowOn(Dispatchers.IO)
    }

    private fun showExpenseDetailsDialog(expenseUid: String) {
        val expense = data.expenses
            .firstOrNull { expense -> expense.uid == expenseUid }
            ?: return

        router.showDialog(
            Dialog.ExpenseDetails(
                ExpenseDetailsDialogArgs(
                    expense = expense
                )
            )
        )
        router.setResultListener(Dialog.ExpenseDetails::class) { action ->
            if (action is ExpenseDetailsAction) {
                when (action) {
                    ExpenseDetailsAction.EDIT ->
                        sendIntent(GroupDetailsIntent.OnEditExpenseClick(expenseUid))

                    ExpenseDetailsAction.REMOVE ->
                        sendIntent(GroupDetailsIntent.OnRemoveExpenseClick(expenseUid))
                }
            }
        }
    }

    private fun showExpenseMenuDialog(expenseUid: String) {
        val items = MenuAction.entries.map { entry ->
            MenuItem(
                icon = entry.icon,
                text = resourceProvider.getString(entry.resourceId),
                actionId = entry.ordinal
            )
        }

        router.showDialog(Dialog.MenuDialog(MenuDialogArgs(items)))
        router.setResultListener(Dialog.MenuDialog::class) { item ->
            if (item is MenuItem) {
                onMenuItemClicked(
                    action = MenuAction.entries.first { action -> action.ordinal == item.actionId },
                    expenseUid = expenseUid
                )
            }
        }
    }

    private fun showRemoveExpenseConfirmationDialog(expenseUid: String) {
        router.showDialog(
            Dialog.ConfirmationDialog(
                args = ConfirmationDialogArgs(
                    message = resourceProvider.getString(
                        R.string.remove_expense_confirmation_message
                    ),
                    buttonTitle = resourceProvider.getString(R.string.remove)
                )
            )
        )
        router.setResultListener(Dialog.ConfirmationDialog::class) { isConfirmed ->
            if (isConfirmed is Boolean && isConfirmed == true) {
                sendIntent(GroupDetailsIntent.OnRemoveExpenseConfirmed(expenseUid))
            }
        }
    }

    private fun onMenuItemClicked(
        action: MenuAction,
        expenseUid: String
    ) {
        when (action) {
            MenuAction.EDIT_EXPENSE -> {
                sendIntent(GroupDetailsIntent.OnEditExpenseClick(expenseUid))
            }

            MenuAction.REMOVE_EXPENSE -> {
                sendIntent(GroupDetailsIntent.OnRemoveExpenseClick(expenseUid))
            }
        }
    }

    private fun showData(group: GroupDto): Flow<GroupDetailsState> {
        return flow<GroupDetailsState> {
            val viewModels = cellFactory.createCells(
                group = group,
                eventProvider = cellEventProvider
            )

            emit(GroupDetailsState.Data(viewModels))
        }.flowOn(Dispatchers.IO)
    }

    private fun getExpenseUidFromCellId(cellId: String): String? {
        return cellId.parseCellId()?.payload?.getStringOrNull()
    }

    enum class MenuAction(
        val icon: Icon,
        @StringRes val resourceId: Int
    ) {
        EDIT_EXPENSE(Icon.EDIT, R.string.edit),
        REMOVE_EXPENSE(Icon.REMOVE, R.string.remove)
    }
}