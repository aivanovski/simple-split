package com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model

import com.github.ai.simplesplit.android.utils.StringUtils

sealed interface CheckoutGroupState {

    data object Loading : CheckoutGroupState

    data class Data(
        val url: String = StringUtils.EMPTY,
        val urlError: String? = null,
        val errorMessage: String? = null
    ) : CheckoutGroupState
}