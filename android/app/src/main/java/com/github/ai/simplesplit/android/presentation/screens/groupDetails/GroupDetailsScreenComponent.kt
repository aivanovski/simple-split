package com.github.ai.simplesplit.android.presentation.screens.groupDetails

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.mvi.attach
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.root.RootViewModel

class GroupDetailsScreenComponent(
    context: ComponentContext,
    rootViewModel: RootViewModel,
    router: Router,
    private val args: GroupDetailsArgs
) : ScreenComponent,
    ComponentContext by context,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: GroupDetailsViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(router, args)
        )[GroupDetailsViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        GroupDetailsScreen(viewModel)
    }
}