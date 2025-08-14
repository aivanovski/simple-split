package com.github.ai.simplesplit.android.presentation.core.compose.navigation

import androidx.fragment.app.DialogFragment
import com.arkivanov.decompose.router.stack.items
import com.arkivanov.decompose.router.stack.pop
import com.arkivanov.decompose.router.stack.push
import com.arkivanov.decompose.router.stack.replaceAll
import com.arkivanov.decompose.router.stack.replaceCurrent
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.github.ai.simplesplit.android.presentation.dialogs.root.BottomSheetRootDialog
import com.github.ai.simplesplit.android.presentation.screens.Screen
import com.github.ai.simplesplit.android.presentation.screens.root.model.StartActivityEvent
import com.github.ai.simplesplit.android.utils.StringUtils
import com.github.ai.simplesplit.android.utils.mutableStateFlow
import java.util.concurrent.ConcurrentHashMap
import kotlin.reflect.KClass
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import timber.log.Timber

interface Router {

    fun bindNavigator(navigator: Navigator)
    fun setRoot(screen: Screen)
    fun navigateTo(screen: Screen)
    fun replaceCurrent(screen: Screen)
    fun showDialog(dialog: Dialog)
    fun exit()
    fun setResultListener(
        screenType: KClass<out ResultOwner>,
        onResult: ResultListener
    )

    fun setResult(
        screenType: KClass<out ResultOwner>,
        result: Any
    )

    fun startActivity(event: StartActivityEvent)
}

fun interface ResultListener {

    fun onResult(result: Any)
}

class RouterImpl : Router {

    private val resultListeners: MutableMap<String, ResultListener> = ConcurrentHashMap()
    private val scope = CoroutineScope(Dispatchers.Main)
    private var navigator by mutableStateFlow<Navigator?>(null)
    private var currentDialog by mutableStateFlow<Dialog?>(null)

    override fun bindNavigator(navigator: Navigator) {
        this.navigator = navigator
    }

    override fun setRoot(screen: Screen) {
        scope.launch {
            removeActiveDialogIfNeed()

            getNavigatorOrThrow().getStackNavigation().replaceAll(screen)
            resultListeners.clear()
        }
    }

    override fun navigateTo(screen: Screen) {
        scope.launch {
            removeActiveDialogIfNeed()

            getNavigatorOrThrow().getStackNavigation().push(screen)
        }
    }

    override fun replaceCurrent(screen: Screen) {
        scope.launch {
            removeActiveDialogIfNeed()

            getNavigatorOrThrow().getStackNavigation().replaceCurrent(screen)
        }
    }

    override fun showDialog(dialog: Dialog) {
        scope.launch {
            removeActiveDialogIfNeed()

            val bottomSheet = BottomSheetRootDialog.newInstance(dialog)
            val fragmentManager = getNavigatorOrThrow().getFragmentManager()
            val key = dialog::class.key()
            currentDialog = dialog
            bottomSheet.show(fragmentManager, key)
        }
    }

    override fun exit() {
        scope.launch {
            if (isDialogActive()) {
                removeActiveDialog()
                return@launch
            }

            removeUnusedResultListeners()

            val activeScreenKey = getActiveScreenKey()

            Timber.d(
                "exit: screenKey=%s, hasListener=%s, listeners.size=%s",
                activeScreenKey,
                resultListeners.containsKey(activeScreenKey),
                resultListeners.size
            )

            resultListeners.remove(activeScreenKey)

            getNavigatorOrThrow().getStackNavigation().pop { isSuccess ->
                if (!isSuccess) {
                    getNavigatorOrThrow().exitNavigation()
                }
            }
        }
    }

    override fun setResultListener(
        screenType: KClass<out ResultOwner>,
        onResult: ResultListener
    ) {
        val key = screenType.key()
        resultListeners[key] = onResult
    }

    override fun setResult(
        screenType: KClass<out ResultOwner>,
        result: Any
    ) {
        val key = screenType.key()

        Timber.d(
            "setResult: screenKey=%s, hasListener=%s, listeners.size=%s",
            key,
            resultListeners.containsKey(key),
            resultListeners.size
        )

        resultListeners.remove(key)?.onResult(result)
    }

    override fun startActivity(event: StartActivityEvent) {
        scope.launch {
            getNavigatorOrThrow().startActivity(event)
        }
    }

    private fun isDialogActive(): Boolean {
        return currentDialog != null
    }

    private fun removeActiveDialog() {
        val dialog = currentDialog ?: return

        val currentDialogKey = dialog::class.key()
        resultListeners.remove(currentDialogKey)

        val fragmentManager = getNavigatorOrThrow().getFragmentManager()
        val key = dialog::class.key()
        val fragment = fragmentManager.findFragmentByTag(key)
        if (fragment != null && fragment is DialogFragment) {
            fragment.dismiss()
        }

        currentDialog = null
    }

    private fun removeActiveDialogIfNeed() {
        if (isDialogActive()) {
            removeActiveDialog()
        }
    }

    private fun removeUnusedResultListeners() {
        val currentScreenKeys = getNavigatorOrThrow().getStack().items
            .map { it.configuration::class.key() }
            .toSet()

        val resultKeys = resultListeners.keys.toList()
        for (resultKey in resultKeys) {
            if (resultKey !in currentScreenKeys) {
                Timber.d(
                    "exit: remove non used listener, key=%s",
                    resultKey
                )
                resultListeners.remove(resultKey)
            }
        }
    }

    private fun getActiveScreenKey(): String {
        return getNavigatorOrThrow().getStack().items.lastOrNull()
            ?.configuration
            ?.let { screen -> screen::class }
            ?.key()
            ?: StringUtils.EMPTY
    }

    private fun getNavigatorOrThrow(): Navigator {
        return navigator ?: throw IllegalStateException("Navigator is not bound")
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