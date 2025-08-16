package com.github.ai.simplesplit.android.domain.usecase

import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.data.settings.Settings

class CreateExportUrlUseCase(
    private val settings: Settings
) {

    fun createUrl(credentials: GroupCredentials): String {
        return "%s/export/%s.csv?password=%s".format(
            settings.serverUrl,
            credentials.groupUid,
            credentials.password
        )
    }
}