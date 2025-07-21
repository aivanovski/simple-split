package com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model

import kotlinx.serialization.Serializable

@Serializable
data class ConfirmationDialogArgs(
    val message: String,
    val buttonTitle: String
)