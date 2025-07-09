package com.github.ai.simplesplit.android.presentation.root

import androidx.fragment.app.FragmentManager
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.essenty.backhandler.BackCallback
import com.github.ai.simplesplit.android.presentation.Screen
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.RouterImpl
import com.github.ai.simplesplit.android.presentation.groups.GroupsScreenComponent

class RootScreenComponent(
    componentContext: ComponentContext,
    onExitNavigation: () -> Unit,
    fragmentManager: FragmentManager
) : ComponentContext by componentContext,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    // TODO: ViewModels instances should be retain in case of screen state restoration

    val navigation = StackNavigation<Screen>()
    val router = RouterImpl(
        rootComponent = this,
        fragmentManager = fragmentManager,
        onExitNavigation = onExitNavigation
    )

    val viewModel: RootViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory(router)
        )[RootViewModel::class]
    }

    val childStack = childStack(
        source = navigation,
        serializer = Screen.serializer(),
        initialStack = { viewModel.getStartScreens() },
        childFactory = { screen, childContext -> createScreenComponent(screen, childContext) }
    )

    private val backCallback = BackCallback(
        isEnabled = true,
        onBack = {
            router.exit()
        }
    )

    init {
        backHandler.register(backCallback)
    }

    private fun createScreenComponent(
        screen: Screen,
        childContext: ComponentContext
    ): ComponentContext {
        return when (screen) {
            is Screen.Groups -> GroupsScreenComponent(
                context = childContext,
                rootViewModel = viewModel,
                router = router
            )
        }
    }
}