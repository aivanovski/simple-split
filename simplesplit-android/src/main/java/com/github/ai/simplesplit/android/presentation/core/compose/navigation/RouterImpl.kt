package com.github.ai.simplesplit.android.presentation.core.compose.navigation

import androidx.fragment.app.FragmentManager
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.github.ai.simplesplit.android.presentation.Screen
import com.github.ai.simplesplit.android.presentation.root.RootScreenComponent
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

class RouterImpl(
    private val rootComponent: RootScreenComponent,
    private val fragmentManager: FragmentManager,
    private val onExitNavigation: () -> Unit
) : Router {

    private val scope = CoroutineScope(Dispatchers.Main)

    override fun setRoot(screen: Screen) {
        scope.launch {
            rootComponent.navigation.replaceAll(screen)
        }
    }

    override fun navigateTo(screen: Screen) {
        scope.launch {
            rootComponent.navigation.push(screen)
        }
    }

    override fun replaceCurrent(screen: Screen) {
        scope.launch {
            rootComponent.navigation.replaceCurrent(screen)
        }
    }

    override fun exit() {
        scope.launch {
            val key = rootComponent.childStack.items.lastOrNull()
                ?.configuration
                ?.let { screen ->
                    screen::class
                }
                ?.key()

            Timber.d("exit: screenKey=%s", key)

            rootComponent.navigation.pop { isSuccess ->
                if (!isSuccess) {
                    onExitNavigation.invoke()
                }
            }
        }
    }

    private fun KClass<*>.key(): String {
        val typeName = this.java.name
        val startIndex = typeName.indexOf(Screen::class.java.simpleName)

        return if (startIndex in typeName.indices) {
            typeName.substring(startIndex)
        } else {
            typeName
        }
    }
}