package com.github.ai.simplesplit.android.presentation.screens.root

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedScreenPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import com.github.ai.simplesplit.android.presentation.screens.root.model.RootIntent

@Composable
fun RootScreen(rootComponent: RootScreenComponent) {
    val viewModel = rootComponent.viewModel

    RootScreen(
        onIntent = viewModel::sendIntent
    ) {
        Children(
            stack = rootComponent.childStack
        ) { (_, component) ->
            Surface(
                modifier = Modifier.fillMaxSize(),
                color = AppTheme.theme.colors.background
            ) {
                CompositionLocalProvider(
                    LocalViewModelStoreOwner.provides(component as ViewModelStoreOwner)
                ) {
                    (component as ScreenComponent).render()
                }
            }
        }
    }
}

@Composable
private fun RootScreen(
    onIntent: (intent: RootIntent) -> Unit,
    content: @Composable () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxSize(),
        color = AppTheme.theme.colors.background
    ) {
        content.invoke()
    }
}

@Composable
@Preview
fun RootScreenLightPreview() {
    ThemedScreenPreview(theme = LightTheme) {
        RootScreen(
            onIntent = {},
            content = {
                Box(
                    contentAlignment = Alignment.Center
                ) {
                    Text("SCREEN CONTENT")
                }
            }
        )
    }
}