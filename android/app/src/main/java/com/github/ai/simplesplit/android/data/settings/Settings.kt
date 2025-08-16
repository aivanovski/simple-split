package com.github.ai.simplesplit.android.data.settings

import android.content.Context
import com.cioccarellia.ksprefs.KsPrefs
import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.settings.SettingKey.HTTP_LOG_LEVEL
import com.github.ai.simplesplit.android.data.settings.SettingKey.IS_SSL_VERIFICATION_ENABLED
import com.github.ai.simplesplit.android.data.settings.SettingKey.SERVER_URL
import com.github.ai.simplesplit.android.utils.StringUtils
import io.ktor.client.plugins.logging.LogLevel

interface Settings {
    var serverUrl: String
    var isSslVerificationEnabled: Boolean
    var httpLogLevel: LogLevel
}

class SettingsImpl(
    context: Context
) : Settings {

    private val prefs = KsPrefs(context.applicationContext)

    override var isSslVerificationEnabled: Boolean
        get() = prefs.pull(IS_SSL_VERIFICATION_ENABLED.key, true)
        set(value) {
            prefs.push(IS_SSL_VERIFICATION_ENABLED.key, value)
        }

    override var serverUrl: String
        get() = prefs.pull(SERVER_URL.key, ApiClient.PROD_SERVER_URL)
        set(value) {
            prefs.push(SERVER_URL.key, value)
        }

    override var httpLogLevel: LogLevel
        get() {
            return prefs.pull<String>(HTTP_LOG_LEVEL.key, StringUtils.EMPTY).let { name ->
                LogLevel.entries.find { level -> level.name == name }
                    ?: LogLevel.INFO
            }
        }
        set(value) {
            prefs.push(HTTP_LOG_LEVEL.key, value.name)
        }
}