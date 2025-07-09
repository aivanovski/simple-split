package com.github.ai.simplesplit.android.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import com.arkivanov.decompose.defaultComponentContext
import com.github.ai.simplesplit.android.di.GlobalInjector.inject
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.root.RootScreen
import com.github.ai.simplesplit.android.presentation.root.RootScreenComponent

class MainActivity : AppCompatActivity() {

    private val themeProvider: ThemeProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeProvider.onThemedContextCreated(this)

        val component = RootScreenComponent(
            componentContext = defaultComponentContext(),
            fragmentManager = supportFragmentManager,
            onExitNavigation = {
                finish()
            }
        )

        setContent {
            AppTheme(theme = themeProvider.theme) {
                RootScreen(
                    rootComponent = component
                )
            }
        }
    }
}