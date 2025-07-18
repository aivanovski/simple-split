package com.github.ai.simplesplit.android.di

import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.api.HttpClientFactory
import com.github.ai.simplesplit.android.data.database.AppDatabase
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.ResourceProviderImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProviderImpl
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.GroupDetailsInteractor
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.GroupDetailsViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.GroupDetailsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.ExpenseEditorInteractor
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.ExpenseEditorViewModel
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.GroupEditorInteractor
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.GroupEditorViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupEditor.model.GroupEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groups.GroupsInteractor
import com.github.ai.simplesplit.android.presentation.screens.groups.GroupsViewModel
import com.github.ai.simplesplit.android.presentation.screens.root.RootViewModel
import org.koin.core.module.dsl.singleOf
import org.koin.dsl.bind
import org.koin.dsl.module

object AndroidAppModule {

    val module = module {
        // Core
        singleOf(::ThemeProviderImpl).bind(ThemeProvider::class)
        singleOf(::ResourceProviderImpl).bind(ResourceProvider::class)

        // Database
        single { AppDatabase.buildDatabase(get()) }
        single { get<AppDatabase>().groupCredentialsDao() }

        // Api
        singleOf(::JsonSerializer)
        single { HttpClientFactory.createHttpClient() }
        singleOf(::ApiClient)

        // Repositories
        singleOf(::GroupRepository)
        singleOf(::GroupCredentialsRepository)

        // Interactors
        singleOf(::GroupsInteractor)
        singleOf(::GroupDetailsInteractor)
        singleOf(::GroupEditorInteractor)
        singleOf(::ExpenseEditorInteractor)

        // CellFactories
        singleOf(::GroupDetailsCellFactory)

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
        factory { (router: Router, args: GroupDetailsArgs) ->
            GroupDetailsViewModel(
                get(),
                get(),
                router,
                args
            )
        }
        factory { (router: Router, args: GroupEditorArgs) ->
            GroupEditorViewModel(
                get(),
                get(),
                router,
                args
            )
        }
        factory { (router: Router, args: ExpenseEditorArgs) ->
            ExpenseEditorViewModel(
                get(),
                get(),
                router,
                args
            )
        }
    }
}