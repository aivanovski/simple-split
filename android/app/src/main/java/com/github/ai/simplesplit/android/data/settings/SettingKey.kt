package com.github.ai.simplesplit.android.data.settings

enum class SettingKey(
    val key: String
) {
    IS_SSL_VERIFICATION_ENABLE(
        key = "isSslVerificationEnabled"
    ),
    SERVER_URL(
        key = "serverUrl"
    )
}