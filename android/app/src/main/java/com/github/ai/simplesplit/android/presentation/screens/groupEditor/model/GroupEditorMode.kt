package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import kotlinx.serialization.Serializable

@Serializable
sealed class GroupEditorMode {
    @Serializable
    data class EditGroup(val credentials: GroupCredentials) : GroupEditorMode()

    @Serializable
    data object NewGroup : GroupEditorMode()
}