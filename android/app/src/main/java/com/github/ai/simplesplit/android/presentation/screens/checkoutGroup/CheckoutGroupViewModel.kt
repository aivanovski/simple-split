package com.github.ai.simplesplit.android.presentation.screens.checkoutGroup

import arrow.core.Either
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.MviViewModel
import com.github.ai.simplesplit.android.presentation.core.mvi.nonStateAction
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupArgs
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupIntent
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupState
import com.github.ai.simplesplit.android.utils.getErrorMessage
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import com.github.ai.simplesplit.android.utils.singleFlowOf
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.flowOn

class CheckoutGroupViewModel(
    private val interactor: CheckoutGroupInteractor,
    private val resourceProvider: ResourceProvider,
    private val router: Router,
    private val args: CheckoutGroupArgs
) : MviViewModel<CheckoutGroupState, CheckoutGroupIntent>(
    initialState = CheckoutGroupState.Loading,
    initialIntent = CheckoutGroupIntent.Initialize
) {

    private var dataState by mutableStateFlow(CheckoutGroupState.Data())

    override fun handleIntent(intent: CheckoutGroupIntent): Flow<CheckoutGroupState> {
        return when (intent) {
            CheckoutGroupIntent.Initialize -> loadData()
            CheckoutGroupIntent.OnBackClick -> nonStateAction { navigateBack() }
            CheckoutGroupIntent.OnDoneClick -> onDoneClicked()
            is CheckoutGroupIntent.OnUrlChanged -> onUrlChanged(intent)
        }
    }

    private fun loadData(): Flow<CheckoutGroupState> {
        return flowOf(dataState)
    }

    private fun onUrlChanged(intent: CheckoutGroupIntent.OnUrlChanged): Flow<CheckoutGroupState> {
        dataState = dataState.copy(
            url = intent.url,
            urlError = null
        )
        return singleFlowOf(dataState)
    }

    private fun onDoneClicked(): Flow<CheckoutGroupState> {
        if (dataState.url.isBlank()) {
            dataState = dataState.copy(
                urlError = resourceProvider.getString(R.string.field_required_error)
            )
            return flowOf(dataState)
        }

        return flow {
            emit(CheckoutGroupState.Loading)
            kotlinx.coroutines.delay(500)

            when (val addGroupResult = interactor.addGroup(url = dataState.url.trim())) {
                is Either.Left -> {
                    dataState = dataState.copy(errorMessage = addGroupResult.getErrorMessage())
                    emit(dataState)
                }

                is Either.Right -> {
                    navigateBack()
                }
            }
        }.flowOn(Dispatchers.IO)
    }

    private fun navigateBack() {
        router.exit()
    }
}