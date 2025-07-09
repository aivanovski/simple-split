package com.github.ai.simplesplit.android.presentation.core.mvi

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe

fun <S, I> Lifecycle.attach(viewModel: MviViewModel<S, I>) {
    this.subscribe(
        onStart = {
            viewModel.start()
        },
        onDestroy = {
            viewModel.destroy()
        }
    )
}