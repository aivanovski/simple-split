package com.github.ai.simplesplit.android.presentation.dialogs.menuDialog

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.DialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.utils.attach

class MenuDialogComponent(
    lifecycle: Lifecycle,
    viewModelStoreOwner: ViewModelStoreOwner,
    args: MenuDialogArgs
) : DialogComponent {

    private val viewModel: MenuDialogViewModel by lazy {
        ViewModelProvider(
            owner = viewModelStoreOwner,
            factory = ViewModelFactory(args)
        )[MenuDialogViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        MenuDialogScreen(viewModel)
    }
}