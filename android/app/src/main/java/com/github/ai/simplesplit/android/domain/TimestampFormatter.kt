package com.github.ai.simplesplit.android.domain

import android.content.Context
import java.text.DateFormat
import java.util.Date
import java.util.Locale

class TimestampFormatter(
    private val context: Context
) {

    fun formatShortDate(timestampMillis: Long): String {
        val formatter = DateFormat.getDateInstance(DateFormat.MEDIUM, getSystemLocale(context))
        return formatter.format(Date(timestampMillis))
    }

    fun formatDateAndTime(timestampMillis: Long): String {
        val dateFormatter = DateFormat.getDateInstance(DateFormat.LONG, getSystemLocale(context))
        val timeFormatter = DateFormat.getTimeInstance()
        val date = Date(timestampMillis)
        return dateFormatter.format(date) + " " + timeFormatter.format(date)
    }

    private fun getSystemLocale(context: Context): Locale {
        return context.resources.configuration.locales[0]
    }
}