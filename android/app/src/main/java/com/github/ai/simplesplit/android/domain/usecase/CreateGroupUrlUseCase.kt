package com.github.ai.simplesplit.android.domain.usecase

import com.github.ai.simplesplit.android.data.settings.Settings
import com.github.ai.simplesplit.android.model.db.GroupCredentials

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