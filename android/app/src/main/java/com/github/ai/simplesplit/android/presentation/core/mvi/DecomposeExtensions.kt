package com.github.ai.simplesplit.android.presentation.core.mvi

import com.arkivanov.essenty.lifecycle.Lifecycle
import com.arkivanov.essenty.lifecycle.subscribe

fun <State, Intent : MviIntent> Lifecycle.attach(viewModel: MviViewModel<State, Intent>) {
    this.subscribe(
        onStart = {
            viewModel.start()
        },
        onDestroy = {
            viewModel.destroy()
        }
    )
}