package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import com.github.ai.simplesplit.android.presentation.core.mvi.MviIntent

sealed class GroupEditorIntent(
    override val isImmediate: Boolean = false
) : MviIntent {

    data object Initialize : GroupEditorIntent()
    data object OnBackClick : GroupEditorIntent()
    data object OnAddMemberClick : GroupEditorIntent()
    data object OnDoneClick : GroupEditorIntent()
    data class OnTitleChanged(val title: String) : GroupEditorIntent(isImmediate = true)
    data class OnPasswordChanged(val password: String) : GroupEditorIntent(isImmediate = true)
    data class OnConfirmPasswordChanged(
        val confirmPassword: String
    ) : GroupEditorIntent(isImmediate = true)
    data class OnMemberChanged(val member: String) : GroupEditorIntent(isImmediate = true)
    data class OnRemoveMemberClick(val memberIndex: Int) : GroupEditorIntent()
    data class OnPasswordToggleClick(val isVisible: Boolean) : GroupEditorIntent()
    data class OnConfirmPasswordToggleClick(val isVisible: Boolean) : GroupEditorIntent()
}