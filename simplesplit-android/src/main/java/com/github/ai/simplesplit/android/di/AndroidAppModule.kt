package com.github.ai.simplesplit.android.di

import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProviderImpl
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidAppModule {

    val module = module {
        singleOf(::ThemeProviderImpl).bind(ThemeProvider::class)
    }
}