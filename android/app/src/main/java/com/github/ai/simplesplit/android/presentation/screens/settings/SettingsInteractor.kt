package com.github.ai.simplesplit.android.presentation.screens.settings

import com.github.ai.simplesplit.android.data.api.ApiClient

class SettingsInteractor(
    private val api: ApiClient
) {

    fun onSslVerificationEnabledChanged() {
        api.updateHttpClient()
    }

    fun onServerUrlChanged() {
        api.updateServerUrl()
    }
}