package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.DialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails.model.ExpenseDetailsDialogArgs
import com.github.ai.simplesplit.android.utils.attach

class ExpenseDetailsDialogComponent(
    lifecycle: Lifecycle,
    viewModelStoreOwner: ViewModelStoreOwner,
    args: ExpenseDetailsDialogArgs
) : DialogComponent {

    private val viewModel: ExpenseDetailsDialogViewModel by lazy {
        ViewModelProvider(
            owner = viewModelStoreOwner,
            factory = ViewModelFactory(args)
        )[ExpenseDetailsDialogViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        ExpenseDetailsDialogScreen(viewModel)
    }
}