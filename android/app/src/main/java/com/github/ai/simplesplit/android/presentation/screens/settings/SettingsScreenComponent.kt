package com.github.ai.simplesplit.android.presentation.screens.settings

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.mvi.attach

class SettingsScreenComponent(
    context: ComponentContext
) : ScreenComponent,
    ComponentContext by context,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: SettingsViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory()
        )[SettingsViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        SettingsScreen(viewModel)
    }
}