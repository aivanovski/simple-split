package com.github.ai.simplesplit.android.presentation.core.compose.preview

import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEvent
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.EventListener

object PreviewEventProvider : CellEventProvider {
    override fun subscribe(
        subscriber: Any,
        listener: EventListener
    ) {
    }

    override fun unsubscribe(subscriber: Any) {
    }

    override fun sendEvent(event: CellEvent) {
    }

    override fun isSubscribed(subscriber: Any): Boolean = false

    override fun clear() {
    }
}