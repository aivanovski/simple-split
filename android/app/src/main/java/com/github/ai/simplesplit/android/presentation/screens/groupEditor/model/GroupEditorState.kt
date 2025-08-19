package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

import com.github.ai.simplesplit.android.model.ErrorMessage
import com.github.ai.simplesplit.android.utils.StringUtils

sealed interface GroupEditorState {
    data object Loading : GroupEditorState

    data class Error(
        val error: ErrorMessage
    ) : GroupEditorState

    data class Data(
        val title: String = StringUtils.EMPTY,
        val password: String = StringUtils.EMPTY,
        val confirmPassword: String = StringUtils.EMPTY,
        val member: String = StringUtils.EMPTY,
        val members: List<MemberItem> = emptyList(),
        val titleError: String? = null,
        val passwordError: String? = null,
        val confirmPasswordError: String? = null,
        val memberError: String? = null,
        val isApplyButtonVisible: Boolean = false,
        val isAddButtonVisible: Boolean = false,
        val isCancelButtonVisible: Boolean = false,
        val isPasswordVisible: Boolean = false,
        val isConfirmPasswordVisible: Boolean = false,
        val error: ErrorMessage? = null
    ) : GroupEditorState
}