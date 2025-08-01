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
import com.github.ai.simplesplit.android.presentation.core.compose.theme.EmptyMessageItemHeight
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HugeMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.ExpenseCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.model.SettlementCellModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.ExpenseCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.viewModel.SettlementCellViewModel
import com.github.ai.simplesplit.android.utils.CellId
import com.github.ai.simplesplit.android.utils.CellIdPayload
import com.github.ai.simplesplit.android.utils.CellIdPayload.StringPayload
import com.github.ai.simplesplit.android.utils.format
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
            models.addAll(createExpenseModels(group))
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
                height = GroupMargin,
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
                height = HalfMargin,
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
                height = GroupMargin,
                shape = CornersShape.BOTTOM
            )
        )
    }

    private fun createExpenseHeaderModels(): List<CellModel> {
        return listOf(
            SpaceCellModel(
                id = "expenses_header_space",
                height = GroupMargin
            ),
            HeaderCellModel(
                id = "expense_header",
                text = resourceProvider.getString(R.string.expenses),
                textSize = TextSize.TITLE_MEDIUM,
                textColor = themeProvider.theme.colors.primaryText
            )
        )
    }

    private fun createEmptyStateModels(): List<CellModel> {
        return listOf(
            EmptyMessageCellModel(
                id = "empty_message",
                message = resourceProvider.getString(R.string.no_expenses_message),
                height = EmptyMessageItemHeight
            )
        )
    }

    private fun createExpenseModels(group: GroupDto): List<CellModel> {
        val models = mutableListOf<CellModel>()

        models.addAll(createExpenseHeaderModels())

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
                    id = CellId("expense", StringPayload(expense.uid)).format(),
                    title = expense.title,
                    description = "Paid by $payerName", // TODO: string
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
                height = GroupMargin
            ),
            HeaderCellModel(
                id = "settlement_header",
                text = "How to Settle Debts", // TODO: string
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

        models.addAll(createSettlementHeaderModels())

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

        if (transactions.isEmpty()) {
            models.add(
                EmptyMessageCellModel(
                    id = "settlement_empty_message",
                    message = resourceProvider.getString(R.string.no_debts_yet),
                    height = HugeMargin
                )
            )
        }

        return models
    }
}