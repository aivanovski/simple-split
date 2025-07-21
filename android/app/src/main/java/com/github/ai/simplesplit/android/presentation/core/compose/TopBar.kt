package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme

enum class TopBarMenuItem {
    DONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    isBackVisible: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    menuItems: List<TopBarMenuItem> = emptyList(),
    onMenuItemClick: ((item: TopBarMenuItem) -> Unit)? = null
) {
    TopAppBar(
        colors = TopAppBarDefaults.topAppBarColors(
            containerColor = AppTheme.theme.colors.background,
            titleContentColor = AppTheme.theme.colors.primaryText
        ),
        title = {
            Text(
                text = title,
                color = AppTheme.theme.colors.primaryText
            )
        },
        navigationIcon = {
            if (isBackVisible && onBackClick != null) {
                IconButton(
                    onClick = onBackClick
                ) {
                    Icon(
                        tint = AppTheme.theme.colors.primaryIcon,
                        imageVector = AppIcons.ArrowBack,
                        contentDescription = null
                    )
                }
            }
        },
        actions = {
            if (menuItems.isNotEmpty()) {
                for (menuItem in menuItems) {
                    when (menuItem) {
                        TopBarMenuItem.DONE -> {
                            IconButton(
                                onClick = {
                                    onMenuItemClick?.invoke(menuItem)
                                }
                            ) {
                                Icon(
                                    imageVector = AppIcons.Check,
                                    tint = AppTheme.theme.colors.primaryIcon,
                                    contentDescription = null
                                )
                            }
                        }
                    }
                }
            }
        }
    )
}