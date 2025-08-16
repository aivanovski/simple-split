package com.github.ai.simplesplit.android.domain.usecase

import com.github.ai.simplesplit.android.data.database.model.GroupCredentials
import com.github.ai.simplesplit.android.data.settings.Settings

class CreateGroupUrlUseCase(
    private val settings: Settings
) {

    fun createUrl(credentials: GroupCredentials): String {
        return "%s/group?ids=%s&passwords=%s".format(
            settings.serverUrl,
            credentials.groupUid,
            credentials.password
        )
    }
}