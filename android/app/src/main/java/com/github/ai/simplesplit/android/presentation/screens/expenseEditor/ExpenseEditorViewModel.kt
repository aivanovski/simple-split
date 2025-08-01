package com.github.ai.simplesplit.android.presentation.screens.expenseEditor

import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.MviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorIntent
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorMode
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.singleFlowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn
import timber.log.Timber

class ExpenseEditorViewModel(
    private val interactor: ExpenseEditorInteractor,
    private val resourceProvider: ResourceProvider,
    private val router: Router,
    private val args: ExpenseEditorArgs
) : MviViewModel<ExpenseEditorState, ExpenseEditorIntent>(
    initialState = ExpenseEditorState.Data(),
    initialIntent = ExpenseEditorIntent.Initialize
) {

    private var dataState by mutableStateFlow(ExpenseEditorState.Data())

    override fun handleIntent(intent: ExpenseEditorIntent): Flow<ExpenseEditorState> {
        return when (intent) {
            ExpenseEditorIntent.Initialize -> initialize()
            ExpenseEditorIntent.OnBackClick -> nonStateAction { navigateBack() }
            ExpenseEditorIntent.OnDoneClick -> onDoneClicked()
            is ExpenseEditorIntent.OnPayerChanged -> onPayerChanged(intent)
            is ExpenseEditorIntent.OnTitleChanged -> onTitleChanged(intent)
            is ExpenseEditorIntent.OnAmountChanged -> onAmountChanged(intent)
        }
    }

    private fun initialize(): Flow<ExpenseEditorState> {
        return flow {
            emit(ExpenseEditorState.Loading)

            val memberNames = args.group.members.map { member -> member.name }

            when (args.mode) {
                ExpenseEditorMode.NewExpense -> {
                    dataState = dataState.copy(
                        payer = memberNames.first(),
                        availablePayers = memberNames
                    )
                }

                is ExpenseEditorMode.EditExpense -> {
                    val expense = args.group.expenses
                        .firstOrNull { expense -> expense.uid == args.mode.expenseUid }

                    val payer = expense?.paidBy
                        ?.firstOrNull()
                        ?.name
                        .orEmpty()

                    dataState = dataState.copy(
                        title = expense?.title.orEmpty(),
                        amount = expense?.amount?.toString().orEmpty(),
                        payer = payer,
                        availablePayers = memberNames
                    )
                }
            }

            emit(dataState)
        }.flowOn(Dispatchers.IO)
    }

    private fun onPayerChanged(
        intent: ExpenseEditorIntent.OnPayerChanged
    ): Flow<ExpenseEditorState> {
        dataState = dataState.copy(
            payer = intent.payer
        )

        return singleFlowOf(dataState)
    }

    private fun onTitleChanged(
        intent: ExpenseEditorIntent.OnTitleChanged
    ): Flow<ExpenseEditorState> {
        dataState = dataState.copy(
            title = intent.title,
            titleError = null
        )

        return singleFlowOf(dataState)
    }

    private fun onAmountChanged(
        intent: ExpenseEditorIntent.OnAmountChanged
    ): Flow<ExpenseEditorState> {
        dataState = dataState.copy(
            amount = intent.amount,
            amountError = null
        )

        return singleFlowOf(dataState)
    }

    private fun navigateBack() {
        router.exit()
    }

    private fun onDoneClicked(): Flow<ExpenseEditorState> {
        if (dataState.title.isBlank()) {
            dataState = dataState.copy(
                titleError = resourceProvider.getString(R.string.enter_expense_title)
            )
            return flowOf(dataState)
        }

        if (dataState.amount.isBlank()) {
            dataState = dataState.copy(
                amountError = resourceProvider.getString(R.string.enter_amount)
            )
            return flowOf(dataState)
        }

        val amount = try {
            dataState.amount.toDouble()
        } catch (e: NumberFormatException) {
            dataState = dataState.copy(
                amountError = resourceProvider.getString(R.string.invalid_amount)
            )
            return flowOf(dataState)
        }

        if (amount <= 0) {
            dataState = dataState.copy(
                amountError = resourceProvider.getString(R.string.amount_must_be_positive)
            )
            return flowOf(dataState)
        }

        val payerName = dataState.payer.trim()
        val payer = args.group.members.firstOrNull { member -> member.name == payerName }
            ?: return flowOf(dataState)

        return flow {
            emit(ExpenseEditorState.Loading)

            val response = when (args.mode) {
                ExpenseEditorMode.NewExpense -> interactor.createExpense(
                    groupUid = args.group.uid,
                    title = dataState.title.trim(),
                    amount = amount,
                    payerUid = payer.uid
                )

                is ExpenseEditorMode.EditExpense -> {
                    interactor.updateExpense(
                        credentials = args.credentials,
                        expenseUid = args.mode.expenseUid,
                        title = dataState.title.trim().ifBlank { null },
                        amount = amount,
                        payerUid = payer.uid
                    )
                }
            }

            if (response.isLeft()) {
                emit(ExpenseEditorState.Error(response.getErrorMessage()))
                return@flow
            }

            val expense = response.getOrNull() ?: return@flow

            // TODO: remove logging
            Timber.d("Successfully: title=${expense.title}, uid=${expense.uid}, amount=$amount")

            router.setResult(Screen.ExpenseEditor::class, expense)
            router.exit()
        }.flowOn(Dispatchers.IO)
    }
}