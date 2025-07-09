package com.github.ai.simplesplit.android.presentation.core.mvi

import androidx.annotation.CallSuper
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.flow.onStart
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.launch

abstract class MviViewModel<State, Intent>(
    initialState: State,
    private val initialIntent: Intent
) : ViewModel() {

    val state = MutableStateFlow(initialState)
    private val intents = Channel<Intent>(capacity = Channel.BUFFERED)
    private val isStarted = MutableStateFlow(false)
    private val blocks = mutableListOf<() -> Unit>()

    init {
        doWhenStarted {
            viewModelScope.launch {
                intents.receiveAsFlow()
                    .onStart { emit(initialIntent) }
                    .flatMapLatest { intent -> handleIntent(intent) }
                    .flowOn(Dispatchers.IO)
                    .collect { newState ->
                        state.value = newState
                    }
            }
        }
    }

    @CallSuper
    fun start() {
        if (!isStarted.value) {
            isStarted.value = true

            for (block in blocks) {
                block.invoke()
            }
            blocks.clear()
        }
    }

    @CallSuper
    fun destroy() {
        isStarted.value = false
        viewModelScope.cancel()
    }

    fun sendIntent(intent: Intent) {
        viewModelScope.launch {
            intents.send(intent)
        }
    }

    protected fun doWhenStarted(block: () -> Unit) {
        if (isStarted.value) {
            block.invoke()
        } else {
            blocks.add(block)
        }
    }

    abstract fun handleIntent(intent: Intent): Flow<State>
}