package com.github.ai.simplesplit.android.di

import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProviderImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.groups.GroupsInteractor
import com.github.ai.simplesplit.android.presentation.groups.GroupsViewModel
import com.github.ai.simplesplit.android.presentation.root.RootViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidAppModule {

    val module = module {
        // Core
        singleOf(::ThemeProviderImpl).bind(ThemeProvider::class)

        // Interactors
        singleOf(::GroupsInteractor)

        // ViewModels
        factory { (router: Router) ->
            RootViewModel(
                router
            )
        }
        factory { (router: Router) ->
            GroupsViewModel(
                get(),
                router
            )
        }
    }
}