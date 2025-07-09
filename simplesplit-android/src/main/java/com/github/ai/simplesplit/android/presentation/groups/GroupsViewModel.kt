package com.github.ai.simplesplit.android.presentation.groups

import com.github.ai.simplesplit.android.domain.model.Group
import com.github.ai.simplesplit.android.presentation.core.compose.navigation.Router
import com.github.ai.simplesplit.android.presentation.core.mvi.MviViewModel
import com.github.ai.simplesplit.android.presentation.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.groups.model.GroupsState
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class GroupsViewModel(
    private val interactor: GroupsInteractor,
    private val router: Router,
) : MviViewModel<GroupsState, GroupsIntent>(
    initialState = GroupsState.Loading,
    initialIntent = GroupsIntent.Initialize
) {

    override fun handleIntent(intent: GroupsIntent): Flow<GroupsState> {
        return when (intent) {
            GroupsIntent.Initialize -> {
                flowOf(
                    GroupsState.Data(
                        groups = listOf(
                            Group(
                                id = "1",
                                title = "Group 1",
                                description = "Description 1"
                            ),
                        )
                    )
                )
            }
        }
    }
}