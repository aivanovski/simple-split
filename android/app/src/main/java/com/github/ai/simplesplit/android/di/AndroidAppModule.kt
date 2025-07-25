package com.github.ai.simplesplit.android.di

import com.github.ai.simplesplit.android.data.api.ApiClient
import com.github.ai.simplesplit.android.data.api.HttpClientFactory
import com.github.ai.simplesplit.android.data.database.AppDatabase
import com.github.ai.simplesplit.android.data.json.JsonSerializer
import com.github.ai.simplesplit.android.data.repository.ExpenseRepository
import com.github.ai.simplesplit.android.data.repository.GroupCredentialsRepository
import com.github.ai.simplesplit.android.data.repository.GroupRepository
import com.github.ai.simplesplit.android.presentation.core.ResourceProvider
import com.github.ai.simplesplit.android.presentation.core.ResourceProviderImpl
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.RouterImpl
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProvider
import com.github.ai.simplesplit.android.presentation.core.compose.theme.ThemeProviderImpl
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.ConfirmationDialogViewModel
import com.github.ai.simplesplit.android.presentation.dialogs.confirmationDialog.model.ConfirmationDialogArgs
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.MenuDialogViewModel
import com.github.ai.simplesplit.android.presentation.dialogs.menuDialog.model.MenuDialogArgs
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.ExpenseEditorInteractor
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.ExpenseEditorViewModel
import com.github.ai.simplesplit.android.presentation.screens.expenseEditor.model.ExpenseEditorArgs
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.GroupDetailsInteractor
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.GroupDetailsViewModel
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.cells.GroupDetailsCellFactory
import com.github.ai.simplesplit.android.presentation.screens.groupDetails.model.GroupDetailsArgs
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
        singleOf(::ExpenseRepository)

        // Interactors
        singleOf(::GroupsInteractor)
        singleOf(::GroupDetailsInteractor)
        singleOf(::GroupEditorInteractor)
        singleOf(::ExpenseEditorInteractor)

        // CellFactories
        singleOf(::GroupDetailsCellFactory)

        // Router
        singleOf(::RouterImpl).bind(Router::class)

        // Screen ViewModels
        factory { RootViewModel(get()) }
        factory {
            GroupsViewModel(
                get(),
                get(),
                get()
            )
        }
        factory { (args: GroupDetailsArgs) ->
            GroupDetailsViewModel(
                get(),
                get(),
                get(),
                args
            )
        }
        factory { (args: GroupEditorArgs) ->
            GroupEditorViewModel(
                get(),
                get(),
                get(),
                args
            )
        }
        factory { (args: ExpenseEditorArgs) ->
            ExpenseEditorViewModel(
                get(),
                get(),
                get(),
                args
            )
        }

        // Dialog ViewModels
        factory { (args: MenuDialogArgs) ->
            MenuDialogViewModel(
                get(),
                args
            )
        }
        factory { (args: ConfirmationDialogArgs) ->
            ConfirmationDialogViewModel(
                get(),
                get(),
                args
            )
        }
    }
}