package com.github.ai.simplesplit.android.presentation.core

import androidx.lifecycle.ViewModelStore
import androidx.lifecycle.ViewModelStoreOwner

class ViewModelStoreOwnerImpl : ViewModelStoreOwner {

    override val viewModelStore: ViewModelStore = ViewModelStore()
}