package com.github.ai.simplesplit.android.presentation.screens.groups.cells

import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.model.GroupCellModel
import com.github.ai.simplesplit.android.presentation.screens.groups.cells.viewModel.GroupCellViewModel
import com.github.ai.split.api.GroupDto

class CellFactory {

    fun createCells(
        groups: List<GroupDto>,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val models = mutableListOf<CellModel>()

        for ((index, group) in groups.withIndex()) {
            val members = group.members
                .map { member -> member.name }
                .joinToString(", ")

            val sum = group.expenses.sumOf { expense -> expense.amount }

            models.add(
                GroupCellModel(
                    id = group.uid,
                    title = group.title,
                    description = group.description,
                    members = members,
                    amount = sum.toString()
                )
            )

            if (index != groups.lastIndex) {
                models.add(
                    SpaceCellModel(
                        id = "space_$index",
                        height = 16.dp
                    )
                )
            }
        }

        return models.map { model ->
            when (model) {
                is SpaceCellModel -> SpaceCellViewModel(model)
                is GroupCellModel -> GroupCellViewModel(model, eventProvider)
                else -> throw IllegalArgumentException("Unknown model type: $model")
            }
        }
    }
}