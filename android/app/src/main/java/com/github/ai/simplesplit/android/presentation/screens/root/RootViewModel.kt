package com.github.ai.simplesplit.android.presentation.screens.root

import androidx.lifecycle.ViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.root.model.RootIntent

class RootViewModel(
    private val router: Router
) : ViewModel() {

    fun getStartScreens(): List<Screen> {
        return listOf(Screen.Groups)
    }

    fun sendIntent(intent: RootIntent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: RootIntent) {
        when (intent) {
            RootIntent.OnBackClick -> {
                router.exit()
            }
        }
    }
}