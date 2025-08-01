package com.github.ai.simplesplit.android.presentation.screens.groupEditor.model

sealed interface ValidationResult {
    data object Valid : ValidationResult
    data class Invalid(
        val titleError: String? = null,
        val passwordError: String? = null,
        val confirmationError: String? = null,
        val membersError: String? = null
    ) : ValidationResult
}