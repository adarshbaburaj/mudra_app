package com.ada.mudra.features.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.data.model.AppSettings
import com.ada.mudra.ui.components.GiantButton

@Composable
fun PinScreen(
    viewModel: MudraViewModel,
    onPinAccepted: () -> Unit,
    onGoBack: () -> Unit,
) {
    val settings by viewModel.settings.collectAsState()
    var pinInput by remember { mutableStateOf("") }
    var wrongPin by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.pin_title),
            style = MaterialTheme.typography.headlineMedium,
        )
        Text(
            text = stringResource(R.string.pin_body),
            style = MaterialTheme.typography.bodyLarge,
        )
        if (settings.caregiverPin == AppSettings.DEFAULT_PIN) {
            Text(
                text = stringResource(R.string.pin_default_hint),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
            )
        }

        OutlinedTextField(
            value = pinInput,
            onValueChange = { value ->
                pinInput = value.filter { it.isDigit() }.take(6)
                wrongPin = false
            },
            label = { Text(stringResource(R.string.pin_field_label)) },
            keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.NumberPassword),
            visualTransformation = PasswordVisualTransformation(),
            isError = wrongPin,
            singleLine = true,
            textStyle = MaterialTheme.typography.bodyLarge,
            modifier = Modifier.fillMaxWidth(),
        )
        if (wrongPin) {
            Text(
                text = stringResource(R.string.pin_wrong),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
            )
        }

        GiantButton(
            label = stringResource(R.string.pin_open),
            onClick = {
                if (pinInput == settings.caregiverPin) {
                    onPinAccepted()
                } else {
                    wrongPin = true
                }
            },
        )
        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }
}
