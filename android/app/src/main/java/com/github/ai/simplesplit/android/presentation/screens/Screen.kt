package com.github.ai.simplesplit.android.presentation.screens

import com.github.ai.simplesplit.android.presentation.core.compose.navigation.ResultOwner
import com.github.ai.simplesplit.android.presentation.screens.checkoutGroup.model.CheckoutGroupArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import kotlinx.serialization.Serializable

@Serializable
sealed interface Screen : ResultOwner {

    @Serializable
    data object Groups : Screen

    @Serializable
    data object Settings : Screen

    @Serializable
    data class GroupDetails(
        val args: GroupDetailsArgs
    ) : Screen

    @Serializable
    data class GroupEditor(
        val args: GroupEditorArgs
    ) : Screen

    @Serializable
    data class ExpenseEditor(
        val args: ExpenseEditorArgs
    ) : Screen

    @Serializable
    data class CheckoutGroup(
        val args: CheckoutGroupArgs
    ) : Screen
}