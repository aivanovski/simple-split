package com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class CheckoutGroupIntent(
    override val isImmediate: Boolean = false
) : MviIntent {
    data object Initialize : CheckoutGroupIntent()
    data object OnBackClick : CheckoutGroupIntent()
    data object OnDoneClick : CheckoutGroupIntent()
    data object OnCloseErrorClick : CheckoutGroupIntent()
    data class OnUrlChanged(val url: String) : CheckoutGroupIntent(isImmediate = true)
}