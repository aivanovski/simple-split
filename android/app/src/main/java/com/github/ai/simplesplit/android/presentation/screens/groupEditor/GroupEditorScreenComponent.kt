package com.github.ai.simplesplit.android.presentation.screens.groupEditor

import androidx.compose.runtime.Composable
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.mvi.attach
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs

class GroupEditorScreenComponent(
    context: ComponentContext,
    private val args: GroupEditorArgs
) : ScreenComponent,
    ComponentContext by context,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    private val viewModel: GroupEditorViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(args)
        )[GroupEditorViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        GroupEditorScreen(viewModel)
    }
}