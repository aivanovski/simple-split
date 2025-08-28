package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency

import androidx.compose.runtime.Composable
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.DialogComponent
import com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency.model.SelectCurrencyDialogArgs
import com.github.ai.simplesplit.android.utils.attach

class SelectCurrencyDialogComponent(
    lifecycle: Lifecycle,
    viewModelStoreOwner: ViewModelStoreOwner,
    args: SelectCurrencyDialogArgs
) : DialogComponent {

    private val viewModel: SelectCurrencyDialogViewModel by lazy {
        ViewModelProvider(
            owner = viewModelStoreOwner,
            factory = ViewModelFactory(args)
        )[SelectCurrencyDialogViewModel::class]
    }

    init {
        lifecycle.attach(viewModel)
    }

    @Composable
    override fun render() {
        SelectCurrencyDialogScreen(viewModel)
    }
}