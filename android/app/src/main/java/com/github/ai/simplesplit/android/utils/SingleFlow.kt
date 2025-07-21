package com.github.ai.simplesplit.android.utils

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.FlowCollector

class SingleFlow<T>(private val value: T) : Flow<T> {

    override suspend fun collect(collector: FlowCollector<T>) {
        collector.emit(value)
    }
}

fun <T> singleFlowOf(value: T): SingleFlow<T> = SingleFlow(value)