package com.github.ai.simplesplit.android.presentation.core.compose.navigation

import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.FragmentManager
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.router.stack.ChildStack
import com.arkivanov.decompose.router.stack.StackNavigator
import com.arkivanov.decompose.value.Value
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.root.RootScreenComponent

interface Navigator {
    fun getStackNavigation(): StackNavigator<Screen>
    fun getStack(): Value<ChildStack<Screen, ComponentContext>>
    fun getFragmentManager(): FragmentManager
    fun exitNavigation()
}

class NavigatorImpl(
    private val rootComponent: RootScreenComponent,
    private val activity: AppCompatActivity
) : Navigator {

    override fun getStackNavigation(): StackNavigator<Screen> = rootComponent.navigation

    override fun getStack(): Value<ChildStack<Screen, ComponentContext>> = rootComponent.childStack

    override fun exitNavigation() = activity.finish()

    override fun getFragmentManager(): FragmentManager = activity.supportFragmentManager
}