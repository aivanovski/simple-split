package com.github.ai.simplesplit.android.presentation.core.compose.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector

object AppIcons {
    val ArrowBack = Icons.AutoMirrored.Filled.ArrowBackIos
    val Add = Icons.Filled.Add
    val Check = Icons.Outlined.Check
    val Menu = Icons.Filled.MoreVert
    val Settings = Icons.Filled.Settings
    val VisibilityOff = Icons.Outlined.VisibilityOff
    val VisibilityOn = Icons.Outlined.Visibility
    val ErrorCircle = Icons.Outlined.ErrorOutline
    val Close = Icons.Filled.Close
    val ExpandMore = Icons.Filled.ExpandMore
    val Edit = Icons.Filled.Edit
    val Remove = Icons.Filled.Delete
}

enum class Icon {
    EDIT,
    REMOVE
}

fun Icon.toImageVector(): ImageVector =
    when (this) {
        Icon.EDIT -> AppIcons.Edit
        Icon.REMOVE -> AppIcons.Remove
    }