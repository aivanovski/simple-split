package com.github.ai.simplesplit.android.presentation.dialogs.expenseDetails

import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.domain.TimestampFormatter
import com.github.ai.simplesplit.android.model.Expense
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.TextColor
import com.github.ai.simplesplit.android.presentation.core.compose.TextSize
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.BottomSheetHeaderCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DividerCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.TextCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.BottomSheetHeaderCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.TextCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ElementMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.GroupMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.OneLineSmallItemHeight
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.TinyMargin
import com.github.ai.simplesplit.android.utils.formatAsMoney

class ExpenseDetailsDialogCellFactory(
    private val themeProvider: ThemeProvider,
    private val resources: ResourceProvider,
    private val timestampFormatter: TimestampFormatter
) {

    fun createCells(
        expense: Expense,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.addAll(createTitleSection(expense, eventProvider))
        cells.addAll(createPayerSection(expense))
        cells.addAll(createSplitSection(expense))
        cells.addAll(createMenuSection(eventProvider))

        return cells
    }

    private fun createTitleSection(
        expense: Expense,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_before_title",
                    height = GroupMargin
                )
            )
        )

        val currency = expense.currency

        // Title
        cells.add(
            BottomSheetHeaderCellViewModel(
                model = BottomSheetHeaderCellModel(
                    id = "title",
                    title = expense.title,
                    description = expense.amount.formatAsMoney(currency),
                    titleTextSize = TextSize.TITLE_MEDIUM,
                    descriptionTextSize = TextSize.TITLE_LARGE,
                    icon = AppIcon.CLOSE.vector
                ),
                eventProvider = eventProvider
            )
        )

        // Amount
        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_after_title",
                    height = TinyMargin
                )
            )
        )

        val created = expense.createdSeconds * 1000L
        val modified = expense.modifiedSeconds * 1000L

        cells.add(
            TextCellViewModel(
                TextCellModel(
                    id = "created_date",
                    text = resources.getString(
                        R.string.create_on_with_str,
                        timestampFormatter.formatDateAndTime(created)
                    ),
                    textSize = TextSize.BODY_LARGE,
                    textColor = TextColor.SECONDARY
                )
            )
        )

        cells.add(
            TextCellViewModel(
                TextCellModel(
                    id = "modified_date",
                    text = resources.getString(
                        R.string.modified_on_with_str,
                        timestampFormatter.formatDateAndTime(modified)
                    ),
                    textSize = TextSize.BODY_LARGE,
                    textColor = TextColor.SECONDARY
                )
            )
        )

        // Divider
        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_before_divider",
                    height = HalfMargin
                )
            )
        )

        cells.add(
            DividerCellViewModel(
                DividerCellModel(
                    id = "divider",
                    padding = 0.dp
                )
            )
        )

        return cells
    }

    private fun createPayerSection(expense: Expense): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_before_payers",
                    height = ElementMargin
                )
            )
        )

        for ((index, payer) in expense.paidBy.withIndex()) {
            cells.add(
                TextCellViewModel(
                    TextCellModel(
                        id = "payer_$index",
                        text = payer.name + " paid " + expense.amount.formatAsMoney(
                            expense.currency
                        ),
                        textSize = TextSize.BODY_LARGE,
                        textColor = TextColor.PRIMARY
                    )
                )
            )
        }

        return cells
    }

    private fun createSplitSection(expense: Expense): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_before_splits",
                    height = ElementMargin
                )
            )
        )

        val currency = expense.currency
        val payerUid = expense.paidBy.firstOrNull()?.uid.orEmpty()
        val debtors = expense.splitBetween.filter { splitMember -> splitMember.uid != payerUid }

        for ((index, splitMember) in debtors.withIndex()) {
            val debtAmount = expense.amount / expense.splitBetween.size

            cells.add(
                TextCellViewModel(
                    TextCellModel(
                        id = "split_$index",
                        text = splitMember.name + " owe " + debtAmount.formatAsMoney(currency),
                        textSize = TextSize.BODY_LARGE,
                        textColor = TextColor.SECONDARY
                    )
                )
            )
        }

        return cells
    }

    private fun createMenuSection(eventProvider: CellEventProvider): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_before_menu",
                    height = GroupMargin
                )
            )
        )

        cells.add(
            DividerCellViewModel(
                DividerCellModel(
                    id = "divider",
                    padding = 0.dp
                )
            )
        )

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_after_menu",
                    height = HalfMargin
                )
            )
        )

        cells.addAll(
            listOf(
                MenuCellViewModel(
                    MenuCellModel(
                        id = CellId.EDIT_MENU.name,
                        icon = AppIcon.EDIT.vector,
                        title = resources.getString(R.string.edit),
                        height = OneLineSmallItemHeight
                    ),
                    eventProvider
                ),
                MenuCellViewModel(
                    MenuCellModel(
                        id = CellId.REMOVE_MENU.name,
                        icon = AppIcon.REMOVE.vector,
                        title = resources.getString(R.string.remove),
                        height = OneLineSmallItemHeight
                    ),
                    eventProvider
                )
            )
        )

        return cells
    }

    enum class CellId {
        EDIT_MENU,
        REMOVE_MENU
    }
}