package com.github.ai.simplesplit.android.presentation.screens.groups.cells

import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel.GroupCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groups.model.GroupsData
import com.github.ai.simplesplit.android.utils.CellId
import com.github.ai.simplesplit.android.utils.CellIdPayload.StringPayload
import com.github.ai.simplesplit.android.utils.format

class CellFactory {

    fun createCells(
        data: GroupsData,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        for ((index, group) in data.groups.withIndex()) {
            val members = group.members
                .map { member -> member.name }
                .joinToString(", ")

            val sum = group.expenses.sumOf { expense -> expense.amount }

            cells.add(
                GroupCellViewModel(
                    GroupCellModel(
                        id = CellId("group", StringPayload(group.uid)).format(),
                        title = group.title,
                        description = group.description,
                        members = members,
                        amount = "%.2f".format(sum)
                    ),
                    eventProvider
                )
            )

            if (index != data.groups.lastIndex) {
                cells.add(
                    SpaceCellViewModel(
                        SpaceCellModel(
                            id = "space_$index",
                            height = 12.dp
                        )
                    )
                )
            }
        }

        return cells
    }
}