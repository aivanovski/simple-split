package com.github.ai.simplesplit.android.presentation.groups

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.domain.model.Group
import com.github.ai.simplesplit.android.presentation.groups.model.GroupsIntent
import com.github.ai.simplesplit.android.presentation.groups.model.GroupsState

@Composable
fun GroupsScreen(
    viewModel: GroupsViewModel
) {
    val state by viewModel.state.collectAsState()

    GroupsScreen(
        state = state,
        onIntent = viewModel::sendIntent
    )
}

@Composable
private fun GroupsScreen(
    state: GroupsState,
    onIntent: (intent: GroupsIntent) -> Unit
) {
    Box {
        when (state) {
            GroupsState.Loading -> {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }

            is GroupsState.Data -> {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                ) {
                    items(state.groups) { group ->
                        GroupItem(
                            group = group,
                            onGroupClick = {}
                        )
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun GroupItem(
    group: Group,
    onGroupClick: () -> Unit
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        onClick = onGroupClick
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(16.dp)
        ) {
            Text(
                text = group.title,
                style = MaterialTheme.typography.headlineSmall
            )

            Spacer(modifier = Modifier.height(4.dp))

            Text(
                text = group.description,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}