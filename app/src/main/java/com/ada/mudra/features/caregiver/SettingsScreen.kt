package com.ada.mudra.features.caregiver

import androidx.appcompat.app.AppCompatDelegate
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.FilterChip
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.core.os.LocaleListCompat
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.core.LauncherMode
import com.ada.mudra.ui.components.GiantButton

private data class LanguageOption(val tag: String, val labelRes: Int)

private val languageOptions = listOf(
    LanguageOption("en", R.string.language_english),
    LanguageOption("ml", R.string.language_malayalam),
    LanguageOption("hi", R.string.language_hindi),
)

@Composable
fun SettingsScreen(
    viewModel: MudraViewModel,
    onGoBack: () -> Unit,
) {
    val context = LocalContext.current
    val settings by viewModel.settings.collectAsState()

    val currentLanguage = AppCompatDelegate.getApplicationLocales()
        .toLanguageTags()
        .ifBlank { "en" }
        .substringBefore("-")

    var newPin by remember { mutableStateOf("") }
    var confirmPin by remember { mutableStateOf("") }
    var pinMessage by remember { mutableStateOf<Int?>(null) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.caregiver_settings),
            style = MaterialTheme.typography.headlineMedium,
        )

        // Haptics
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = settings.hapticsEnabled,
                onCheckedChange = { viewModel.setHapticsEnabled(it) },
            )
            Text(
                text = stringResource(R.string.settings_haptics),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }

        HorizontalDivider()

        // Language
        Text(
            text = stringResource(R.string.settings_language),
            style = MaterialTheme.typography.titleLarge,
        )
        Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
            languageOptions.forEach { option ->
                FilterChip(
                    selected = currentLanguage == option.tag,
                    onClick = {
                        AppCompatDelegate.setApplicationLocales(
                            LocaleListCompat.forLanguageTags(option.tag)
                        )
                    },
                    label = {
                        Text(
                            text = stringResource(option.labelRes),
                            style = MaterialTheme.typography.bodyMedium,
                        )
                    },
                )
            }
        }

        HorizontalDivider()

        // PIN change
        Text(
            text = stringResource(R.string.settings_change_pin),
            style = MaterialTheme.typography.titleLarge,
        )
        OutlinedTextField(
            value = newPin,
            onValueChange = { value -> newPin = value.filter { it.isDigit() }.take(6) },
            label = { Text(stringResource(R.string.settings_new_pin)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        OutlinedTextField(
            value = confirmPin,
            onValueChange = { value -> confirmPin = value.filter { it.isDigit() }.take(6) },
            label = { Text(stringResource(R.string.settings_confirm_pin)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        pinMessage?.let { messageRes ->
            Text(
                text = stringResource(messageRes),
                style = MaterialTheme.typography.bodyLarge,
                color = if (messageRes == R.string.settings_pin_saved) {
                    MaterialTheme.colorScheme.primary
                } else {
                    MaterialTheme.colorScheme.error
                },
            )
        }
        GiantButton(
            label = stringResource(R.string.settings_change_pin),
            onClick = {
                pinMessage = when {
                    newPin.length !in 4..6 -> R.string.settings_pin_invalid
                    newPin != confirmPin -> R.string.settings_pin_mismatch
                    else -> {
                        viewModel.changePin(newPin)
                        newPin = ""
                        confirmPin = ""
                        R.string.settings_pin_saved
                    }
                }
            },
        )

        HorizontalDivider()

        // Optional home-screen (launcher) mode
        Row(verticalAlignment = Alignment.CenterVertically) {
            Switch(
                checked = settings.launcherModeEnabled,
                onCheckedChange = { enabled ->
                    viewModel.setLauncherModeEnabled(enabled)
                    LauncherMode.setEnabled(context, enabled)
                },
            )
            Text(
                text = stringResource(R.string.settings_launcher),
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.padding(start = 12.dp),
            )
        }
        Text(
            text = stringResource(R.string.settings_launcher_hint),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        HorizontalDivider()

        Text(
            text = stringResource(R.string.settings_privacy),
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }
}
