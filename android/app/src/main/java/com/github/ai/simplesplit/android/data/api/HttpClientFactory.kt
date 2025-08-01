package com.github.ai.simplesplit.android.data.api

import com.github.ai.simplesplit.android.data.json.JsonSerializer
import io.ktor.client.HttpClient
import io.ktor.client.engine.okhttp.OkHttp
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logger
import io.ktor.client.plugins.logging.Logging
import io.ktor.serialization.kotlinx.json.json
import timber.log.Timber

object HttpClientFactory {
    fun createHttpClient(jsonSerializer: JsonSerializer): HttpClient {
        return HttpClient(OkHttp) {
            install(ContentNegotiation) {
                json(jsonSerializer.json)
            }
            install(Logging) {
                logger = object : Logger {
                    override fun log(message: String) {
                        Timber.d(message)
                    }
                }
                level = LogLevel.BODY
            }
        }
    }
}