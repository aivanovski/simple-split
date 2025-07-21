package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.DialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.utils.attach

class ConfirmationDialogComponent(
    lifecycle: Lifecycle,
    viewModelStoreOwner: ViewModelStoreOwner,
    args: ConfirmationDialogArgs
) : DialogComponent {

    private val viewModel: ConfirmationDialogViewModel by lazy {
        ViewModelProvider(
            owner = viewModelStoreOwner,
            factory = ViewModelFactory(args)
        )[ConfirmationDialogViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        ConfirmationDialogScreen(viewModel)
    }
}