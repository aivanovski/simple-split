package com.github.ai.simplesplit.android.presentation.core.mvi

import androidx.annotation.CallSuper
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProviderImpl

abstract class CellsMviViewModel<State, Intent : MviIntent>(
    initialState: State,
    initialIntent: Intent
) : MviViewModel<State, Intent>(initialState, initialIntent) {

    val cellEventProvider = CellEventProviderImpl()

    override fun start() {
        super.start()
        cellEventProvider.subscribe(this) { event -> handleCellEvent(event) }
    }

    @CallSuper
    override fun destroy() {
        super.destroy()
        cellEventProvider.clear()
    }

    abstract fun handleCellEvent(event: CellEvent)
}