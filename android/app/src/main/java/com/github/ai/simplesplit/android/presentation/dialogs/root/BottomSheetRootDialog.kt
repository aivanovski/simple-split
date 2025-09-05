package com.github.ai.simplesplit.android.presentation.dialogs.root

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.compose.foundation.layout.Box
import androidx.compose.ui.Modifier
import androidx.compose.ui.input.nestedscroll.nestedScroll
import androidx.compose.ui.platform.ViewCompositionStrategy
import androidx.compose.ui.platform.rememberNestedScrollInteropConnection
import arrow.core.getOrElse
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.data.json.ArgumentSerializer
import com.github.ai.simplesplit.android.databinding.ComposeViewBinding
import com.github.ai.simplesplit.android.di.GlobalInjector.inject
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.dialogs.Dialog
import com.google.android.material.bottomsheet.BottomSheetDialogFragment

class BottomSheetRootDialog : BottomSheetDialogFragment() {

    private val themeProvider: ThemeProvider by inject()
    private val serializer: ArgumentSerializer by inject()

    private val dialogArgs: Dialog by lazy {
        val args = arguments?.getString(ARGS)
        serializer.deserialize<Dialog>(args ?: "").getOrElse {
            throw IllegalArgumentException("Dialog args not found")
        }
    }

    override fun getTheme(): Int = R.style.BottomSheetDialog

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        val binding = ComposeViewBinding.inflate(inflater, container, false)

        val component = RootDialogComponent(
            lifecycle = viewLifecycleOwner.lifecycle,
            viewModelStoreOwner = this
        )

        val dialogComponent = component.createDialogComponent(dialogArgs)

        binding.composeView.apply {
            setViewCompositionStrategy(ViewCompositionStrategy.DisposeOnViewTreeLifecycleDestroyed)
            setContent {
                AppTheme(theme = themeProvider.theme) {
                    Box(
                        modifier = Modifier.nestedScroll(rememberNestedScrollInteropConnection())
                    ) {
                        dialogComponent.render()
                    }
                }
            }
        }

        return binding.root
    }

    companion object {
        private const val ARGS = "args"

        fun newInstance(dialog: Dialog): BottomSheetDialogFragment {
            val fragment = BottomSheetRootDialog()

            val args = Bundle()
                .apply {
                    // TODO: should be optimized
                    putString(ARGS, fragment.serializer.serialize(dialog))
                }

            fragment.arguments = args

            return fragment
        }
    }
}