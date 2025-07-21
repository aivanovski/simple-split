package com.github.ai.simplesplit.android.presentation.dialogs.root

import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelStoreOwner
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.DialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.ConfirmationDialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.MenuDialogComponent

class RootDialogComponent(
    private val lifecycle: Lifecycle,
    private val viewModelStoreOwner: ViewModelStoreOwner
) {

    fun createDialogComponent(dialog: Dialog): DialogComponent {
        return when (dialog) {
            is Dialog.MenuDialog -> MenuDialogComponent(
                viewModelStoreOwner = viewModelStoreOwner,
                lifecycle = lifecycle,
                args = dialog.args
            )

            is Dialog.ConfirmationDialog -> ConfirmationDialogComponent(
                viewModelStoreOwner = viewModelStoreOwner,
                lifecycle = lifecycle,
                args = dialog.args
            )
        }
    }
}