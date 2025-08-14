package com.github.ai.simplesplit.android.presentation

import android.content.Intent
import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.arkivanov.decompose.defaultComponentContext
import com.github.ai.simplesplit.android.di.GlobalInjector.inject
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.NavigatorImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.event.collectWithLifecycle
import com.github.ai.simplesplit.android.presentation.screens.root.RootScreen
import com.github.ai.simplesplit.android.presentation.screens.root.RootScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.root.RootViewModel
import com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
import com.github.ai.simplesplit.android.utils.IntentUtils.newOpenUrlIntent
import com.github.ai.simplesplit.android.utils.IntentUtils.newShareUrlIntent
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private val themeProvider: ThemeProvider by inject()
    private val router: Router by inject()
    private val viewModel: RootViewModel by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = RootScreenComponent(
            componentContext = defaultComponentContext()
        )

        val navigator = NavigatorImpl(
            rootComponent = component,
            activity = this,
            viewModel = viewModel
        )
        router.bindNavigator(navigator)
        themeProvider.onThemedContextCreated(this)

        lifecycleScope.launch {
            viewModel.events.collectWithLifecycle(lifecycle) { event ->
                when (event) {
                    is StartActivityEvent.OpenUrl -> {
                        val intent = Intent.createChooser(newOpenUrlIntent(event.url), null)
                        startActivity(intent)
                    }

                    is StartActivityEvent.ShareUrl -> {
                        val intent = Intent.createChooser(newShareUrlIntent(event.url), null)
                        startActivity(intent)
                    }
                }
            }
        }

        setContent {
            AppTheme(theme = themeProvider.theme) {
                RootScreen(
                    rootComponent = component
                )
            }
        }
    }
}