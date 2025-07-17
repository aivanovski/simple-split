package com.github.ai.simplesplit.android.presentation.screens

import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import kotlinx.serialization.Serializable

@Serializable
sealed class Screen {

    @Serializable
    data object Groups : Screen()

    @Serializable
    data class GroupDetails(
        val args: GroupDetailsArgs
    ) : Screen()

    @Serializable
    data class GroupEditor(
        val args: GroupEditorArgs
    ) : Screen()
}