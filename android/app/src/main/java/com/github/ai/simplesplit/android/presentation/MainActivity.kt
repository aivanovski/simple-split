package com.github.ai.simplesplit.android.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.github.ai.simplesplit.android.di.GlobalInjector.inject
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.NavigatorImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.screens.root.RootScreen
import com.github.ai.simplesplit.android.presentation.screens.root.RootScreenComponent

class MainActivity : AppCompatActivity() {

    private val themeProvider: ThemeProvider by inject()
    private val router: Router by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        val component = RootScreenComponent(
            componentContext = defaultComponentContext()
        )

        router.bindNavigator(NavigatorImpl(component, this))
        themeProvider.onThemedContextCreated(this)

        setContent {
            AppTheme(theme = themeProvider.theme) {
                RootScreen(
                    rootComponent = component
                )
            }
        }
    }
}