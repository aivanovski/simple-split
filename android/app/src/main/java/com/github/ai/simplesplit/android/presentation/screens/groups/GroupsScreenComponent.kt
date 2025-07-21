package com.github.ai.simplesplit.android.presentation.screens.groups

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.mvi.attach

class GroupsScreenComponent(
    context: ComponentContext
) : ScreenComponent,
    ComponentContext by context,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: GroupsViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory()
        )[GroupsViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        GroupsScreen(viewModel)
    }
}