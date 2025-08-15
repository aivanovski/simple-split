package com.github.ai.simplesplit.android.data.settings

import android.content.Context
import com.cioccarellia.ksprefs.KsPrefs
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.settings.SettingKey.IS_SSL_VERIFICATION_ENABLE
import com.github.ai.simplesplit.android.data.settings.SettingKey.SERVER_URL

interface Settings {
    var serverUrl: String
    var isSslVerificationEnabled: Boolean
}

class SettingsImpl(
    context: Context
) : Settings {

    private val prefs = KsPrefs(context.applicationContext)

    override var isSslVerificationEnabled: Boolean
        get() = prefs.pull(IS_SSL_VERIFICATION_ENABLE.key, false)
        set(value) {
            prefs.push(IS_SSL_VERIFICATION_ENABLE.key, value)
        }

    override var serverUrl: String
        get() = prefs.pull(SERVER_URL.key, ApiClient.PROD_SERVER_URL)
        set(value) {
            prefs.push(SERVER_URL.key, value)
        }
}