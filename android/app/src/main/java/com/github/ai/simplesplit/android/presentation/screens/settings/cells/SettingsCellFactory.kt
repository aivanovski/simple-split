package com.github.ai.simplesplit.android.presentation.screens.settings.cells

import com.github.ai.simplesplit.android.R
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.settings.Settings
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellEventProvider
import com.github.ai.simplesplit.android.presentation.core.compose.cells.CellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.DropDownCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.model.SwitchCellModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.DropDownCellViewModel
import com.github.ai.simplesplit.android.presentation.core.compose.cells.viewModel.SwitchCellViewModel
import io.ktor.client.plugins.logging.LogLevel

class SettingsCellFactory(
    private val resources: ResourceProvider
) {

    fun createCells(
        settings: Settings,
        eventProvider: CellEventProvider
    ): List<CellViewModel> {
        val cells = mutableListOf<CellViewModel>()

        cells.add(
            DropDownCellViewModel(
                DropDownCellModel(
                    id = SettingsCellId.SERVER_URL.name,
                    title = resources.getString(R.string.server_url),
                    options = listOf(ApiClient.PROD_SERVER_URL, ApiClient.DEBUG_SERVER_URL),
                    selectedOption = settings.serverUrl
                ),
                eventProvider
            )
        )

        val logOptions = LogLevel.entries.map { level -> level.name }

        cells.add(
            DropDownCellViewModel(
                DropDownCellModel(
                    id = SettingsCellId.HTTP_LOG_LEVEL.name,
                    title = resources.getString(R.string.http_log_level),
                    options = logOptions,
                    selectedOption = settings.httpLogLevel.name
                ),
                eventProvider
            )
        )

        cells.add(
            SwitchCellViewModel(
                SwitchCellModel(
                    id = SettingsCellId.SSL_CERTIFICATE_SWITCH.name,
                    title = resources.getString(R.string.validate_ssl_certificate_title),
                    description = resources.getString(
                        R.string.validate_ssl_certificate_description
                    ),
                    isChecked = settings.isSslVerificationEnabled,
                    isEnabled = true
                ),
                eventProvider
            )
        )

        return cells
    }

    enum class SettingsCellId {
        SERVER_URL,
        SSL_CERTIFICATE_SWITCH,
        HTTP_LOG_LEVEL
    }
}