package com.github.ai.simplesplit.android.presentation

import android.os.Bundle
import androidx.activity.compose.setContent
import androidx.appcompat.app.AppCompatActivity
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Text
import androidx.compose.ui.Modifier
import com.github.ai.simplesplit.android.di.GlobalInjector.inject
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider

class MainActivity : AppCompatActivity() {

    private val themeProvider: ThemeProvider by inject()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        themeProvider.onThemedContextCreated(this)

        setContent {
            AppTheme(theme = themeProvider.theme) {
                Box(
                    modifier = Modifier.fillMaxSize()
                ) {
                    Text("Hello!")
                }
            }
        }
    }
}