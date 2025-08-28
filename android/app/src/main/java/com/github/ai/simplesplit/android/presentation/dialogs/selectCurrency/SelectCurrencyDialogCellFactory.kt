package com.github.ai.simplesplit.android.presentation.dialogs.selectCurrency

import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.data.database.model.CurrencyEntity
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DividerCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.MenuCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SpaceCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DividerCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.MenuCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SpaceCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.HalfMargin
import com.github.ai.simplesplit.android.presentation.core.compose.theme.OneLineMediumItemHeight
import com.github.ai.simplesplit.android.utils.CellId
import com.github.ai.simplesplit.android.utils.CellIdPayload.StringPayload
import com.github.ai.simplesplit.android.utils.format

class SelectCurrencyDialogCellFactory {

    fun createCells(
        currencies: List<CurrencyEntity>,
        query: String,
        selectedIsoCode: String?,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        val isoCodeToCurrencyMap = currencies.associateBy { currency -> currency.isoCode }
        val usdCurrency = isoCodeToCurrencyMap["USD"]
        val euroCurrency = isoCodeToCurrencyMap["EUR"]

        cells.add(
            SpaceCellViewModel(
                SpaceCellModel(
                    id = "space_top",
                    height = HalfMargin
                )
            )
        )

        cells.add(
            DividerCellViewModel(
                DividerCellModel(
                    id = "divider_before_popular",
                    padding = 0.dp
                )
            )
        )

        // Popular currencies
        if (query.isBlank() && (usdCurrency != null || euroCurrency != null)) {
            if (usdCurrency != null) {
                cells.add(
                    createCurrencyCell(
                        idPrefix = POPULAR_SECTION_PREFIX,
                        currency = usdCurrency,
                        selectedIsoCode = selectedIsoCode,
                        eventProvider = eventProvider
                    )
                )
            }

            if (euroCurrency != null) {
                cells.add(
                    createCurrencyCell(
                        idPrefix = POPULAR_SECTION_PREFIX,
                        currency = euroCurrency,
                        selectedIsoCode = selectedIsoCode,
                        eventProvider = eventProvider
                    )
                )
            }

            cells.add(
                DividerCellViewModel(
                    DividerCellModel(
                        id = "divider_after_popular",
                        padding = 0.dp
                    )
                )
            )
        }

        // All currencies
        for (currency in currencies) {
            cells.add(
                createCurrencyCell(
                    idPrefix = CURRENCY_SECTION_PREFIX,
                    currency = currency,
                    selectedIsoCode = selectedIsoCode,
                    eventProvider = eventProvider
                )
            )
        }

        return cells
    }

    private fun createCurrencyCell(
        idPrefix: String,
        currency: CurrencyEntity,
        selectedIsoCode: String?,
        eventProvider: CellEventProvider
    ): CellViewModel {
        val title = "${currency.name} - ${currency.symbol}"
        val icon = if (currency.isoCode == selectedIsoCode) {
            AppIcon.CHECK.vector
        } else {
            null
        }

        return MenuCellViewModel(
            MenuCellModel(
                id = CellId(idPrefix, StringPayload(currency.isoCode)).format(),
                icon = icon,
                title = title,
                height = OneLineMediumItemHeight
            ),
            eventProvider
        )
    }

    companion object {
        const val CURRENCY_SECTION_PREFIX = "currencies"
        const val POPULAR_SECTION_PREFIX = "popular_currencies"
    }
}