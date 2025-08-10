package com.github.ai.simplesplit.android.presentation.screens.root

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.ViewModelStoreOwner
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.StackNavigation
import com.arkivanov.decompose.router.stack.childStack
import com.arkivanov.essenty.backhandler.BackCallback
import com.github.ai.simplesplit.android.presentation.core.ViewModelFactory
import com.github.ai.simplesplit.android.presentation.core.ViewModelStoreOwnerImpl
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.CheckoutGroupScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.ExpenseEditorScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.GroupDetailsScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.GroupEditorScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.groups.GroupsScreenComponent
import com.github.ai.simplesplit.android.presentation.screens.root.model.RootIntent

class RootScreenComponent(
    componentContext: ComponentContext
) : ComponentContext by componentContext,
    ViewModelStoreOwner by ViewModelStoreOwnerImpl() {

    val navigation = StackNavigation<Screen>()

    val viewModel: RootViewModel by lazy {
        ViewModelProvider(
            owner = this,
            factory = ViewModelFactory()
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
            viewModel.sendIntent(RootIntent.OnBackClick)
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
                context = childContext
            )

            is Screen.GroupDetails -> GroupDetailsScreenComponent(
                context = childContext,
                args = screen.args
            )

            is Screen.GroupEditor -> GroupEditorScreenComponent(
                context = childContext,
                args = screen.args
            )

            is Screen.ExpenseEditor -> ExpenseEditorScreenComponent(
                context = childContext,
                args = screen.args
            )

            is Screen.CheckoutGroup -> CheckoutGroupScreenComponent(
                context = childContext,
                args = screen.args
            )
        }
    }
}