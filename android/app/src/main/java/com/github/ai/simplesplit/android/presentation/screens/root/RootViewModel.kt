package com.github.ai.simplesplit.android.presentation.screens.root

import androidx.lifecycle.ViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.event.Event
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.root.model.RootIntent
import com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
import kotlinx.coroutines.flow.MutableStateFlow

class RootViewModel(
    private val router: Router
) : ViewModel() {

    val events = MutableStateFlow<Event<StartActivityEvent>>(Event.empty())

    fun getStartScreens(): List<Screen> {
        return listOf(Screen.Groups)
    }

    fun sendIntent(intent: RootIntent) {
        handleIntent(intent)
    }

    private fun handleIntent(intent: RootIntent) {
        when (intent) {
            RootIntent.OnBackClick -> router.exit()
            is RootIntent.StartActivity -> {
                events.value = Event(intent.event)
            }
        }
    }
}