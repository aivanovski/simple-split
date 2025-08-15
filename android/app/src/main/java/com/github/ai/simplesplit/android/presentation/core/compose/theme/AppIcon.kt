package com.github.ai.simplesplit.android.presentation.core.compose.theme

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBackIos
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.AddLink
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.ArrowDropUp
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.ExpandMore
import androidx.compose.material.icons.filled.FileUpload
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Share
import androidx.compose.material.icons.outlined.Check
import androidx.compose.material.icons.outlined.ErrorOutline
import androidx.compose.material.icons.outlined.Visibility
import androidx.compose.material.icons.outlined.VisibilityOff
import androidx.compose.ui.graphics.vector.ImageVector

enum class AppIcon {
    ARROW_BACK,
    ADD,
    LINK,
    CHECK,
    MENU,
    SETTINGS,
    VISIBILITY_OFF,
    VISIBILITY_ON,
    ERROR_CIRCLE,
    CLOSE,
    EXPAND_MORE,
    EDIT,
    REMOVE,
    EXPORT,
    SHARE,
    ARROW_DROP_DOWN,
    ARROW_DROP_UP;

    val vector: ImageVector
        get() =
            when (this) {
                ARROW_BACK -> Icons.AutoMirrored.Filled.ArrowBackIos
                ADD -> Icons.Filled.Add
                LINK -> Icons.Filled.AddLink
                CHECK -> Icons.Outlined.Check
                MENU -> Icons.Filled.MoreVert
                SETTINGS -> Icons.Filled.Settings
                VISIBILITY_OFF -> Icons.Outlined.VisibilityOff
                VISIBILITY_ON -> Icons.Outlined.Visibility
                ERROR_CIRCLE -> Icons.Outlined.ErrorOutline
                CLOSE -> Icons.Filled.Close
                EXPAND_MORE -> Icons.Filled.ExpandMore
                EDIT -> Icons.Filled.Edit
                REMOVE -> Icons.Filled.Delete
                EXPORT -> Icons.Filled.FileUpload
                SHARE -> Icons.Filled.Share
                ARROW_DROP_DOWN -> Icons.Filled.ArrowDropDown
                ARROW_DROP_UP -> Icons.Filled.ArrowDropUp
            }
}