package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency

import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.CellsMviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogIntent
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogState
import com.github.ai.simplesplit.android.utils.SingleFlow
import com.github.ai.simplesplit.android.utils.StringUtils
import com.github.ai.simplesplit.android.utils.getStringOrNull
import com.github.ai.simplesplit.android.utils.parseCellId
import com.github.ai.simplesplit.android.utils.singleFlowOf
import com.github.ai.simplesplit.android.utils.toErrorMessage
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf

class SelectCurrencyDialogViewModel(
    private val interactor: SelectCurrencyInteractor,
    private val cellFactory: SelectCurrencyDialogCellFactory,
    private val resources: ResourceProvider,
    private val router: Router,
    private val args: SelectCurrencyDialogArgs
) : CellsMviViewModel<SelectCurrencyDialogState, SelectCurrencyDialogIntent>(
    initialState = SelectCurrencyDialogState.Data(isLoading = true),
    initialIntent = SelectCurrencyDialogIntent.Initialize
) {

    private var allCurrencies by mutableStateOf<List<CurrencyEntity>?>(null)

    override fun handleIntent(intent: SelectCurrencyDialogIntent): Flow<SelectCurrencyDialogState> {
        return when (intent) {
            SelectCurrencyDialogIntent.Initialize -> loadData()
            SelectCurrencyDialogIntent.Dismiss -> nonStateAction { navigateBack() }
            SelectCurrencyDialogIntent.OnResetQueryClick -> onResetQueryClicked()
            is SelectCurrencyDialogIntent.OnQueryTextChange -> onQueryTextChanged(intent.query)
            is SelectCurrencyDialogIntent.FilterCurrencies -> filterData(intent.query)

            is SelectCurrencyDialogIntent.OnCurrencySelected ->
                nonStateAction { onCurrencySelected(intent.currencyIsoCode) }
        }
    }

    override fun handleCellEvent(event: CellEvent) {
        when (event) {
            is MenuCellEvent.OnClick -> {
                val currencyIsoCode = event.cellId.parseCellId()
                    ?.payload
                    ?.getStringOrNull()

                if (currencyIsoCode != null) {
                    sendIntent(SelectCurrencyDialogIntent.OnCurrencySelected(currencyIsoCode))
                }
            }
        }
    }

    private fun loadData(): Flow<SelectCurrencyDialogState> {
        return flow {
            emit(SelectCurrencyDialogState.Data(isLoading = true))

            interactor.getCurrencies()
                .fold(
                    ifLeft = { error ->
                        val message = error.toErrorMessage(resources)
                        emit(SelectCurrencyDialogState.Error(message))
                    },
                    ifRight = { currencies ->
                        allCurrencies = currencies

                        val state = state.value.asData().copy(
                            isLoading = false,
                            cellViewModels = buildCells(currencies, StringUtils.EMPTY)
                        )

                        emit(state)
                    }
                )
        }
    }

    private fun onResetQueryClicked(): Flow<SelectCurrencyDialogState> {
        val currencies = allCurrencies ?: return emptyFlow()

        return flowOf(
            state.value.asData().copy(
                query = StringUtils.EMPTY,
                isLoading = false,
                cellViewModels = buildCells(currencies, StringUtils.EMPTY)
            )
        )
    }

    private fun onQueryTextChanged(query: String): SingleFlow<SelectCurrencyDialogState> {
        sendIntent(SelectCurrencyDialogIntent.FilterCurrencies(query))

        return singleFlowOf(
            state.value.asData().copy(
                isLoading = true,
                query = query
            )
        )
    }

    private fun filterData(query: String): Flow<SelectCurrencyDialogState> {
        val currencies = allCurrencies ?: return emptyFlow()
        val trimmedQuery = query.trim()

        return flow {
            delay(300L)

            val filteredCurrencies = currencies.filter { currency ->
                currency.name.contains(trimmedQuery, ignoreCase = true) ||
                    currency.isoCode.contains(trimmedQuery, ignoreCase = true) ||
                    currency.symbol.contains(trimmedQuery, ignoreCase = true)
            }

            val newState = state.value.asData().copy(
                isLoading = false,
                cellViewModels = if (filteredCurrencies.isNotEmpty()) {
                    buildCells(filteredCurrencies, query)
                } else {
                    emptyList()
                }
            )

            emit(newState)
        }
    }

    private fun buildCells(
        currencies: List<CurrencyEntity>,
        query: String
    ): List<CellViewModel> {
        return cellFactory.createCells(
            currencies = currencies,
            query = query,
            selectedIsoCode = args.selectedIsoCurrencyCode,
            eventProvider = cellEventProvider
        )
    }

    private fun navigateBack() {
        router.exit()
    }

    private fun onCurrencySelected(isoCode: String) {
        val currency = allCurrencies
            ?.firstOrNull { currency -> currency.isoCode == isoCode }
            ?: return

        router.setResult(Dialog.SelectCurrency::class, currency)
        router.exit()
    }

    private fun SelectCurrencyDialogState.asData(): SelectCurrencyDialogState.Data {
        return this as SelectCurrencyDialogState.Data
    }
}