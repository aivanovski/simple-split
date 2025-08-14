package com.github.ai.simplesplit.android.presentation.core.event

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

suspend fun <T> Flow<Event<T>>.collectWithLifecycle(
    lifecycle: Lifecycle,
    collector: FlowCollector<T>
) {
    val events = this

    lifecycle.repeatOnLifecycle(Lifecycle.State.STARTED) {
        events.collect { event ->
            val content = event.getContentIfNotHandled()
            if (content != null) {
                collector.emit(content)
            }
        }
    }
}

class Event<out T>(
    private val content: T
) {

    private var isHandled = false

    fun getContentIfNotHandled(): T? {
        return if (isHandled) {
            null
        } else {
            isHandled = true
            content
        }
    }

    companion object {

        @Suppress("UNCHECKED_CAST")
        fun <T> empty(): Event<T> {
            return Event(Unit).apply {
                isHandled = true
            } as Event<T>
        }
    }
}