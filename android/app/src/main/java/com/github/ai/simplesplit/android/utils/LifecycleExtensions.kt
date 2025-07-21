package com.github.ai.simplesplit.android.utils

import androidx.lifecycle.DefaultLifecycleObserver
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.LifecycleOwner
import com.github.ai.simplesplit.android.presentation.core.mvi.ScreenViewModel

fun Lifecycle.attach(viewModel: ScreenViewModel) {
    this.addObserver(object : DefaultLifecycleObserver {
        override fun onStart(owner: LifecycleOwner) {
            viewModel.start()
        }

        override fun onDestroy(owner: LifecycleOwner) {
            viewModel.destroy()
        }
    })
}