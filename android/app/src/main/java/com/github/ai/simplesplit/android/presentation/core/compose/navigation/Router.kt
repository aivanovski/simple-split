package com.github.ai.simplesplit.android.presentation.core.compose.navigation

import com.github.ai.simplesplit.android.presentation.screens.Screen

interface Router {
    fun setRoot(screen: Screen)
    fun navigateTo(screen: Screen)
    fun replaceCurrent(screen: Screen)
    fun exit()
}