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

            val group = when (args.mode) {
                is ExpenseEditorMode.NewExpense -> args.mode.group
            }

            val memberNames = group.members.map { it.name }

            dataState = dataState.copy(
                payer = memberNames.first(),
                availablePayers = memberNames
            )

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
        // TODO: load first payer in the list

//        if (dataState.payer.isBlank()) {
//            dataState = dataState.copy(
//                payerError = resourceProvider.getString(R.string.select_payer)
//            )
//            return flowOf(dataState)
//        }

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

        val group = when (args.mode) {
            is ExpenseEditorMode.NewExpense -> args.mode.group
        }

        val payerName = dataState.payer.trim()
        val payer = group.members.firstOrNull { member -> member.name == payerName }
            ?: return flowOf(dataState)

        return flow {
            emit(ExpenseEditorState.Loading)

            val createExpenseResult = interactor.createExpense(
                groupUid = group.uid,
                title = dataState.title.trim(),
                amount = amount,
                payerUid = payer.uid
            )

            if (createExpenseResult.isLeft()) {
                val message = createExpenseResult.getErrorMessage()
                emit(ExpenseEditorState.Error(message))
                return@flow
            }

            Timber.d("Successfully created expense: title=${dataState.title}, amount=$amount")

            val expense = createExpenseResult.getOrNull() ?: return@flow

            router.setResult(Screen.ExpenseEditor::class, expense.expense)
            router.exit()
        }.flowOn(Dispatchers.IO)
    }
}