package com.github.ai.simplesplit.android.data.settings

enum class SettingKey(
    val key: String
) {
    IS_SSL_VERIFICATION_ENABLED(key = "isSslVerificationEnabled"),
    SERVER_URL(key = "serverUrl"),
    HTTP_LOG_LEVEL(key = "httpLogLevel")
}