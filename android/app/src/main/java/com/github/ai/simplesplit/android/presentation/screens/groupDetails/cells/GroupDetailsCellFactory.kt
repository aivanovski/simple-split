package com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells

import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.CornersShape
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DividerCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.EmptyMessageCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.HeaderCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ShapedSpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.ShapedTextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.EmptyMessageCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.HeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedSpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.ShapedTextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HugeMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.TinyMargin
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.ExpenseCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.SettlementCellViewModel
import com.github.ai.split.api.GroupDto

class GroupDetailsCellFactory(
    private val themeProvider: ThemeProvider,
    private val resourceProvider: ResourceProvider
) {

    fun createCells(
        group: GroupDto,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val models = mutableListOf<CellModel>()

        models.addAll(createTitleModels(group))
        if (group.expenses.isNotEmpty()) {
            models.addAll(createExpenseHeaderModels())
            models.addAll(createExpenseModels(group))
            models.addAll(createSettlementHeaderModels())
            models.addAll(createSettlementModels(group))
            models.add(createBottomSpaceModel())
        } else {
            models.addAll(createEmptyStateModels())
        }

        return models.map { model ->
            when (model) {
                is SpaceCellModel -> SpaceCellViewModel(model)
                is ShapedSpaceCellModel -> ShapedSpaceCellViewModel(model)
                is ShapedTextCellModel -> ShapedTextCellViewModel(model)
                is HeaderCellModel -> HeaderCellViewModel(model)
                is ExpenseCellModel -> ExpenseCellViewModel(model, eventProvider)
                is SettlementCellModel -> SettlementCellViewModel(model, eventProvider)
                is DividerCellModel -> DividerCellViewModel(model)
                is EmptyMessageCellModel -> EmptyMessageCellViewModel(model)
                else -> throw IllegalArgumentException("Unknown model type: $model")
            }
        }
    }

    private fun createTitleModels(group: GroupDto): List<CellModel> {
        val members = group.members.map { member -> member.name }
            .joinToString(" - ")

        return listOf(
            SpaceCellModel(
                id = "top_space",
                height = HalfMargin
            ),
            ShapedSpaceCellModel(
                id = "title_top_title",
                height = ElementMargin,
                shape = CornersShape.TOP
            ),
            ShapedTextCellModel(
                id = "title",
                text = group.title,
                textColor = themeProvider.theme.colors.primaryText,
                textSize = TextSize.TITLE_LARGE,
                shape = CornersShape.NONE
            ),
            ShapedSpaceCellModel(
                id = "title_middle_space",
                height = TinyMargin,
                shape = CornersShape.NONE
            ),
            ShapedTextCellModel(
                id = "members",
                text = members,
                textColor = themeProvider.theme.colors.secondaryText,
                textSize = TextSize.BODY_LARGE,
                shape = CornersShape.NONE
            ),
            ShapedSpaceCellModel(
                id = "title_bottom_space",
                height = ElementMargin,
                shape = CornersShape.BOTTOM
            )
        )
    }

    private fun createExpenseHeaderModels(): List<CellModel> {
        return listOf(
            SpaceCellModel(
                id = "expenses_header_space",
                height = HalfMargin
            ),
            HeaderCellModel(
                id = "expense_header",
                text = "Expenses",
                textSize = TextSize.TITLE_MEDIUM,
                textColor = themeProvider.theme.colors.primaryText
            )
        )
    }

    private fun createEmptyStateModels(): List<CellModel> {
        return listOf(
            EmptyMessageCellModel(
                id = "empty_message",
                message = resourceProvider.getString(R.string.no_expenses_message)

            )
        )
    }

    private fun createExpenseModels(group: GroupDto): List<CellModel> {
        val models = mutableListOf<CellModel>()

        val expenseCount = group.expenses.size

        for ((idx, expense) in group.expenses.withIndex()) {
            val payerName = expense.paidBy.firstOrNull()?.name.orEmpty()

            val members = expense.splitBetween
                .mapNotNull { user -> user.name.firstOrNull()?.toString() }

            val shape = when {
                expenseCount == 1 -> CornersShape.ALL
                idx == 0 -> CornersShape.TOP
                idx == group.expenses.lastIndex -> CornersShape.BOTTOM
                else -> CornersShape.NONE
            }

            if (idx > 0) {
                models.add(
                    DividerCellModel(
                        id = "divider_$idx",
                        padding = ElementMargin
                    )
                )
            }

            models.add(
                ExpenseCellModel(
                    id = expense.uid,
                    title = expense.title,
                    description = "Paid by $payerName",
                    members = members,
                    amount = expense.amount.toString(),
                    // TODO: date should be implemented on server side
                    date = "01 Jan",
                    shape = shape
                )
            )
        }

        return models
    }

    private fun createSettlementHeaderModels(): List<CellModel> {
        return listOf(
            SpaceCellModel(
                id = "settlements_header_space",
                height = HalfMargin
            ),
            HeaderCellModel(
                id = "settlement_header",
                text = "How to Settle Debts",
                textSize = TextSize.TITLE_MEDIUM,
                textColor = themeProvider.theme.colors.primaryText
            )
        )
    }

    private fun createBottomSpaceModel(): CellModel {
        return SpaceCellModel(
            id = "bottom_space",
            height = HugeMargin
        )
    }

    private fun createSettlementModels(group: GroupDto): List<CellModel> {
        val models = mutableListOf<CellModel>()

        val userUidToUserMap = group.members.associateBy { user -> user.uid }
        val transactions = group.paybackTransactions

        for ((idx, transaction) in transactions.withIndex()) {
            val creditor = userUidToUserMap[transaction.creditorUid] ?: continue
            val debtor = userUidToUserMap[transaction.debtorUid] ?: continue

            val shape = when {
                transactions.size == 1 -> CornersShape.ALL
                idx == 0 -> CornersShape.TOP
                idx == transactions.lastIndex -> CornersShape.BOTTOM
                else -> CornersShape.NONE
            }

            if (idx > 0) {
                models.add(
                    DividerCellModel(
                        id = "settlement_divider_$idx",
                        padding = ElementMargin
                    )
                )
            }

            models.add(
                SettlementCellModel(
                    id = "settlement_$idx",
                    title = "${debtor.name} → ${creditor.name}",
                    amount = "%.2f".format(transaction.amount),
                    shape = shape
                )
            )
        }

        return models
    }
}