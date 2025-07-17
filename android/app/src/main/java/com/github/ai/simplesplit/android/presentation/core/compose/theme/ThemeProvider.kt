package com.github.ai.simplesplit.android.presentation.core.compose.theme

import android.content.Context
import android.content.res.Configuration
import com.github.ai.simplesplit.android.utils.mutableStateFlow

interface ThemeProvider {
    val theme: Theme
    fun onThemedContextCreated(context: Context)
}

class ThemeProviderImpl : ThemeProvider {

    private var isDark by mutableStateFlow(false)

    override val theme: Theme
        get(): Theme {
            return if (isDark) {
                DarkTheme
            } else {
                LightTheme
            }
        }

    override fun onThemedContextCreated(context: Context) {
        isDark = isDarkTheme(context)
    }

    private fun isDarkTheme(context: Context): Boolean {
        val mode = context.resources.configuration.uiMode and Configuration.UI_MODE_NIGHT_MASK
        return mode == Configuration.UI_MODE_NIGHT_YES
    }
}