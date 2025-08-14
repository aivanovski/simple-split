package com.github.ai.simplesplit.android.domain.usecase

import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.model.db.GroupCredentials

class CreateExportUrlUseCase {

    fun createUrl(credentials: GroupCredentials): String {
        return "%s/export/%s.csv?password=%s".format(
            ApiClient.SERVER_URL,
            credentials.groupUid,
            credentials.password
        )
    }
}