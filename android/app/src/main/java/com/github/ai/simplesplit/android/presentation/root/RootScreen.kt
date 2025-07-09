package com.github.ai.simplesplit.android.presentation.root

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.tooling.preview.Preview
import androidx.lifecycle.ViewModelStoreOwner
import androidx.lifecycle.viewmodel.compose.LocalViewModelStoreOwner
import com.arkivanov.decompose.extensions.compose.stack.Children
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.root.model.RootIntent
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ScreenComponent

@Composable
fun RootScreen(rootComponent: RootScreenComponent) {
    val context = LocalContext.current
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
                    LocalViewModelStoreOwner provides component as ViewModelStoreOwner
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
    Scaffold(
        // topBar = {
        //     TopBarContent(
        //         state = topBarState,
        //         menuState = menuState,
        //         onIntent = onIntent
        //     )
        // },
        // bottomBar = {
        //     BottomBarContent(
        //         state = bottomBarState,
        //         onIntent = onIntent
        //     )
        // }
    ) { padding ->
        Surface(
            modifier = Modifier
                .fillMaxSize()
                .padding(
                    top = padding.calculateTopPadding(),
                    bottom = padding.calculateBottomPadding()
                ),
            color = AppTheme.theme.colors.background
        ) {
            content.invoke()
        }
    }
}


@Composable
@Preview
fun RootScreenLightPreview() {
    // ThemedScreenPreview(theme = LightTheme) {
    //     RootScreen(
    //         topBarState = newTopBarState(),
    //         bottomBarState = newBottomBarState(),
    //         menuState = newMenuState(),
    //         onIntent = {},
    //         content = {
    //             Box(
    //                 contentAlignment = Alignment.Center
    //             ) {
    //                 Text("SCREEN CONTENT")
    //             }
    //         }
    //     )
    // }
}