package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import androidx.annotation.StringRes
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
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
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorMode
import com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
import com.github.ai.simplesplit.android.utils.getStringOrNull
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.parseCellId
import com.github.ai.simplesplit.android.utils.toErrorMessage
import com.github.ai.split.api.GroupDto
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class GroupDetailsViewModel(
    private val interactor: GroupDetailsInteractor,
    private val cellFactory: GroupDetailsCellFactory,
    private val resources: ResourceProvider,
    private val router: Router,
    private val args: GroupDetailsArgs
) : CellsMviViewModel<GroupDetailsState, GroupDetailsIntent>(
    initialState = GroupDetailsState.Loading,
    initialIntent = GroupDetailsIntent.Initialize
) {

    private var data by mutableStateFlow(args.group)

    override fun start() {
        super.start()
        sendIntent(GroupDetailsIntent.ReloadData)
    }

    override fun handleIntent(intent: GroupDetailsIntent): Flow<GroupDetailsState> {
        return when (intent) {
            GroupDetailsIntent.Initialize -> loadData(args.group.uid, args.password)

            GroupDetailsIntent.ReloadData -> loadData(args.group.uid, args.password)

            GroupDetailsIntent.ReloadDataInBackground ->
                loadData(args.group.uid, args.password, isShowLoading = false)

            GroupDetailsIntent.OnBackClick -> nonStateAction { navigateBack() }

            GroupDetailsIntent.OnFabClick ->
                nonStateAction { navigateToNewExpenseScreen() }

            is GroupDetailsIntent.OnExpenseClick ->
                nonStateAction { showExpenseDetailsDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnExpenseLongClick ->
                nonStateAction { showExpenseMenuDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnEditExpenseClick ->
                nonStateAction { navigateToExpenseEditor(intent.expenseUid) }

            is GroupDetailsIntent.OnRemoveExpenseClick ->
                nonStateAction { showRemoveExpenseConfirmationDialog(intent.expenseUid) }

            is GroupDetailsIntent.OnRemoveExpenseConfirmed -> removeExpense(intent.expenseUid)

            GroupDetailsIntent.OnRemoveGroupConfirmed -> removeGroup(args.group.uid)

            GroupDetailsIntent.OnCloseErrorClick -> onCloseErrorClicked()

            is GroupDetailsIntent.OnMenuClick ->
                nonStateAction { showGroupMenuDialog() }

            GroupDetailsIntent.OnEditGroupClick ->
                nonStateAction { navigateToGroupEditor() }

            GroupDetailsIntent.OnRemoveGroupClick ->
                nonStateAction { showRemoveGroupConfirmationDialog() }

            is GroupDetailsIntent.OpenUrl ->
                nonStateAction { router.startActivity(StartActivityEvent.OpenUrl(intent.url)) }

            is GroupDetailsIntent.ShareGroupUrl ->
                nonStateAction { router.startActivity(StartActivityEvent.ShareUrl(intent.url)) }
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

    private fun navigateToExpenseEditor(expenseUid: String) {
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

    private fun navigateToGroupEditor() {
        val creds = GroupCredentials(
            groupUid = args.group.uid,
            password = args.password
        )

        router.navigateTo(
            Screen.GroupEditor(
                GroupEditorArgs(
                    mode = GroupEditorMode.EditGroup(creds)
                )
            )
        )
    }

    private fun navigateBack() {
        router.exit()
    }

    private fun loadData(
        groupUid: String,
        password: String,
        isShowLoading: Boolean = true
    ): Flow<GroupDetailsState> {
        return flow {
            if (isShowLoading) {
                emit(GroupDetailsState.Loading)
            }

            interactor.getGroup(
                groupUid = groupUid,
                password = password
            ).fold(
                ifLeft = { error ->
                    emit(GroupDetailsState.Error(error.toErrorMessage(resources)))
                },
                ifRight = { group ->
                    data = group
                    emit(createScreenState(group))
                }
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun removeExpense(expenseUid: String): Flow<GroupDetailsState> {
        return flow {
            emit(GroupDetailsState.Loading)

            interactor.removeExpense(
                password = args.password,
                expenseUid = expenseUid
            ).fold(
                ifLeft = { error ->
                    val newState = when (val state = state.value) {
                        is GroupDetailsState.Data -> state.copy(
                            error = error.toErrorMessage(resources)
                        )

                        else -> GroupDetailsState.Error(error.toErrorMessage(resources))
                    }

                    emit(newState)
                },
                ifRight = { group ->
                    data = group
                    emit(createScreenState(group))
                }
            )
        }.flowOn(Dispatchers.IO)
    }

    private fun removeGroup(groupUid: String): Flow<GroupDetailsState> {
        return flow<GroupDetailsState> {
            emit(GroupDetailsState.Loading)

            interactor.removeGroup(groupUid)

            router.exit()
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

    private fun showGroupMenuDialog() {
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
                val action = GroupMenuAction.entries.first { it.ordinal == item.actionId }
                onGroupMenuItemClicked(action = action)
            }
        }
    }

    private fun showExpenseMenuDialog(expenseUid: String) {
        val items = ExpenseMenuAction.entries.map { entry ->
            MenuItem(
                icon = entry.icon,
                text = resources.getString(entry.resourceId),
                actionId = entry.ordinal
            )
        }

        router.showDialog(Dialog.MenuDialog(MenuDialogArgs(items)))
        router.setResultListener(Dialog.MenuDialog::class) { item ->
            if (item is MenuItem) {
                val action = ExpenseMenuAction.entries.first { action ->
                    action.ordinal == item.actionId
                }

                onExpenseMenuItemClicked(
                    action = action,
                    expenseUid = expenseUid
                )
            }
        }
    }

    private fun showRemoveExpenseConfirmationDialog(expenseUid: String) {
        router.showDialog(
            Dialog.ConfirmationDialog(
                args = ConfirmationDialogArgs(
                    message = resources.getString(
                        R.string.remove_expense_confirmation_message
                    ),
                    buttonTitle = resources.getString(R.string.remove)
                )
            )
        )
        router.setResultListener(Dialog.ConfirmationDialog::class) { isConfirmed ->
            if (isConfirmed is Boolean && isConfirmed == true) {
                sendIntent(GroupDetailsIntent.OnRemoveExpenseConfirmed(expenseUid))
            }
        }
    }

    private fun showRemoveGroupConfirmationDialog() {
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
                sendIntent(GroupDetailsIntent.OnRemoveGroupConfirmed)
            }
        }
    }

    private fun onGroupMenuItemClicked(action: GroupMenuAction) {
        when (action) {
            GroupMenuAction.EDIT_GROUP -> sendIntent(GroupDetailsIntent.OnEditGroupClick)
            GroupMenuAction.REMOVE_GROUP -> sendIntent(GroupDetailsIntent.OnRemoveGroupClick)
            GroupMenuAction.EXPORT_TO_CSV -> {
                val url = interactor.createExportToCsvUrl(
                    GroupCredentials(
                        groupUid = args.group.uid,
                        password = args.password
                    )
                )

                sendIntent(GroupDetailsIntent.OpenUrl(url))
            }

            GroupMenuAction.SHARE_LINK -> {
                val url = interactor.createShareUrl(
                    GroupCredentials(
                        groupUid = args.group.uid,
                        password = args.password
                    )
                )

                sendIntent(GroupDetailsIntent.ShareGroupUrl(url))
            }
        }
    }

    private fun onExpenseMenuItemClicked(
        action: ExpenseMenuAction,
        expenseUid: String
    ) {
        when (action) {
            ExpenseMenuAction.EDIT_EXPENSE -> {
                sendIntent(GroupDetailsIntent.OnEditExpenseClick(expenseUid))
            }

            ExpenseMenuAction.REMOVE_EXPENSE -> {
                sendIntent(GroupDetailsIntent.OnRemoveExpenseClick(expenseUid))
            }
        }
    }

    private fun createScreenState(group: GroupDto): GroupDetailsState {
        val viewModels = cellFactory.createCells(
            group = group,
            eventProvider = cellEventProvider
        )

        return GroupDetailsState.Data(viewModels)
    }

    private fun onCloseErrorClicked(): Flow<GroupDetailsState> {
        val state = state.value.asDataOrNull() ?: return emptyFlow()

        return flowOf(state.copy(error = null))
    }

    private fun getExpenseUidFromCellId(cellId: String): String? {
        return cellId.parseCellId()?.payload?.getStringOrNull()
    }

    private fun GroupDetailsState.asDataOrNull(): GroupDetailsState.Data? =
        this as? GroupDetailsState.Data

    enum class GroupMenuAction(
        val icon: AppIcon,
        @StringRes val resourceId: Int
    ) {
        EDIT_GROUP(AppIcon.EDIT, R.string.edit),
        REMOVE_GROUP(AppIcon.REMOVE, R.string.remove),
        EXPORT_TO_CSV(AppIcon.EXPORT, R.string.export_as_csv_file),
        SHARE_LINK(AppIcon.SHARE, R.string.share)
    }

    enum class ExpenseMenuAction(
        val icon: AppIcon,
        @StringRes val resourceId: Int
    ) {
        EDIT_EXPENSE(AppIcon.EDIT, R.string.edit),
        REMOVE_EXPENSE(AppIcon.REMOVE, R.string.remove)
    }
}