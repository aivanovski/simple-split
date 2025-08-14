package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcon
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme

@Composable
fun AppDropdownField(
    value: String,
    label: String,
    options: List<String>,
    error: String? = null,
    onValueChange: (String) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }
    val isError = (error != null)

    Column {
        OutlinedTextField(
            value = value,
            onValueChange = { },
            label = {
                Text(label)
            },
            isError = isError,
            readOnly = true,
            trailingIcon = {
                Icon(
                    imageVector = AppIcon.EXPAND_MORE.vector,
                    contentDescription = null,
                    modifier = Modifier.clickable { expanded = true }
                )
            },
            modifier = modifier.clickable { expanded = true }
        )

        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false },
            modifier = Modifier.fillMaxWidth()
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option) },
                    onClick = {
                        onValueChange(option)
                        expanded = false
                    }
                )
            }
        }

        if (isError) {
            Text(
                text = error.orEmpty(),
                color = MaterialTheme.colorScheme.error,
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.padding(start = 16.dp, top = 4.dp)
            )
        }
    }
}

@Preview
@Composable
fun AppDropdownFieldPreview() {
    ThemedPreview(theme = LightTheme) {
        AppDropdownField(
            value = "John Doe",
            label = "Payer",
            options = listOf("John Doe", "Jane Smith", "Bob Johnson"),
            onValueChange = {},
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 16.dp)
        )
    }
}