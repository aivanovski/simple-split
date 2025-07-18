package com.github.ai.simplesplit.android.presentation.core.mvi

import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.emptyFlow

fun <T> nonStateAction(action: () -> Unit): Flow<T> {
    action.invoke()
    return emptyFlow()
}