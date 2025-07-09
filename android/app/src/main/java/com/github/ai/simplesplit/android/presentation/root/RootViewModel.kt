package com.github.ai.simplesplit.android.presentation.root

import androidx.lifecycle.ViewModel
import com.github.ai.simplesplit.android.presentation.Screen
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.root.model.RootIntent

class RootViewModel(
    private val router: Router,
) : ViewModel() {

    fun getStartScreens(): List<Screen> {
        return listOf(Screen.Groups)
    }

    fun sendIntent(intent: RootIntent) {
        // TODO:
    }
}