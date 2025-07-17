package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import com.github.ai.split.api.GroupDto
import kotlinx.serialization.Serializable

@Serializable
sealed class GroupEditorMode {
    @Serializable
    data class EditGroup(val group: GroupDto) : GroupEditorMode()
    
    @Serializable
    data object NewGroup : GroupEditorMode()
} 