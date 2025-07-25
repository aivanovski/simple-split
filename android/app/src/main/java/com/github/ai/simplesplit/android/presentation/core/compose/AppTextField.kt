package com.github.ai.simplesplit.android.presentation.core.compose

import androidx.compose.foundation.clickable
import androidx.compose.foundation.interaction.MutableInteractionSource
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.ripple.rememberRipple
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.github.ai.simplesplit.android.presentation.core.compose.preview.ThemedPreview
import com.github.ai.simplesplit.android.presentation.core.compose.theme.AppIcons
import com.github.ai.simplesplit.android.presentation.core.compose.theme.LightTheme
import kotlinx.coroutines.flow.MutableStateFlow

@Composable
fun AppTextField(
    value: String,
    label: String,
    error: String? = null,
    onValueChange: (String) -> Unit,
    isPasswordToggleEnabled: Boolean = false,
    isPasswordVisible: Boolean = false,
    keyboardOptions: KeyboardOptions = KeyboardOptions.Default,
    onPasswordToggleClicked: ((isPasswordVisible: Boolean) -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    // TODO: resolve issue with inner state
    val innerValue = remember {
        MutableStateFlow(value)
    }
    innerValue.value = value

    val onChange = rememberCallback { newValue: String ->
        innerValue.value = newValue
        onValueChange.invoke(newValue)
    }

    val observedInnerValue by innerValue.collectAsState()

    val isError = (error != null)

    OutlinedTextField(
        value = observedInnerValue,
        onValueChange = onChange,
        label = {
            Text(label)
        },
        isError = isError,
        visualTransformation = if (isPasswordToggleEnabled && !isPasswordVisible) {
            PasswordVisualTransformation()
        } else {
            VisualTransformation.None
        },
        supportingText = {
            if (isError) {
                Text(
                    text = error.orEmpty(),
                    color = MaterialTheme.colorScheme.error
                )
            }
        },
        trailingIcon = {
            when {
                isError -> {
                    Icon(
                        imageVector = AppIcons.ErrorCircle,
                        contentDescription = null
                    )
                }

                isPasswordToggleEnabled -> {
                    val icon = if (isPasswordVisible) {
                        AppIcons.VisibilityOff
                    } else {
                        AppIcons.VisibilityOn
                    }

                    Icon(
                        imageVector = icon,
                        contentDescription = null,
                        modifier = Modifier
                            .clickable(
                                interactionSource = remember { MutableInteractionSource() },
                                indication = rememberRipple(bounded = false)
                            ) {
                                onPasswordToggleClicked?.invoke(!isPasswordVisible)
                            }
                            .padding(8.dp)
                    )
                }
            }
        },
        keyboardOptions = keyboardOptions,
        modifier = modifier
    )
}

@Composable
@Preview
fun TextFieldsLightPreview() {
    ThemedPreview(theme = LightTheme) {
        Column {
            AppTextField(
                value = "",
                label = "Username",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            AppTextField(
                value = "john.doe",
                label = "Username",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            AppTextField(
                value = "abc123",
                label = "Password",
                onValueChange = {},
                isPasswordVisible = false,
                isPasswordToggleEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            AppTextField(
                value = "abc123",
                label = "Password",
                onValueChange = {},
                isPasswordVisible = true,
                isPasswordToggleEnabled = true,
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )

            AppTextField(
                value = "john.doe",
                label = "Username",
                error = "username is already exists",
                onValueChange = {},
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(horizontal = 16.dp)
            )
        }
    }
}