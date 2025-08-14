package com.github.ai.simplesplit.android.utils

import android.content.Intent
import android.net.Uri

object IntentUtils {

    fun newOpenUrlIntent(url: String): Intent {
        return Intent(Intent.ACTION_VIEW).apply {
            data = Uri.parse(url)
        }
    }

    fun newShareUrlIntent(url: String): Intent {
        return Intent(Intent.ACTION_SEND)
            .apply {
                putExtra(Intent.EXTRA_TEXT, url)
                type = "text/plain"
            }
    }
}