package com.github.ai.simplesplit.android.presentation.groups

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.mvi.attach
import com.github.ai.simplesplit.android.presentation.root.RootViewModel

class GroupsScreenComponent(
    context: ComponentContext,
    rootViewModel: RootViewModel,
    router: Router,
) : ScreenComponent,
    ComponentContext by context,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: GroupsViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(router)
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