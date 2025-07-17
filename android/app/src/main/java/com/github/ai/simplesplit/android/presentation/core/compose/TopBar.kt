package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.heightIn
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppTheme

enum class MenuItem {
    DONE
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TopBar(
    title: String,
    isBackVisible: Boolean = false,
    onBackClick: (() -> Unit)? = null,
    menuItems: List<MenuItem> = emptyList(),
    onMenuItemClick: ((item: MenuItem) -> Unit)? = null
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
                        MenuItem.DONE -> {
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