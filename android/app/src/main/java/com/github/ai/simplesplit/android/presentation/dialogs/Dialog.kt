package com.github.ai.simplesplit.android.presentation.dialogs

import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ResultOwner
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import kotlinx.serialization.Serializable

@Serializable
sealed interface Dialog : ResultOwner {

    @Serializable
    data class MenuDialog(
        val args: MenuDialogArgs
    ) : Dialog

    @Serializable
    data class ConfirmationDialog(
        val args: ConfirmationDialogArgs
    ) : Dialog
}