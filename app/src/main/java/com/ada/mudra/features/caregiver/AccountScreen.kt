package com.ada.mudra.features.caregiver

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.data.remote.SyncManager
import com.ada.mudra.ui.components.GiantButton

/**
 * Caregiver-facing account screen: sign in / create account, create the
 * family, and see sync status. The senior never sees this screen.
 */
@Composable
fun AccountScreen(
    viewModel: MudraViewModel,
    onGoBack: () -> Unit,
) {
    val syncState by viewModel.syncState.collectAsState()
    val syncError by viewModel.syncError.collectAsState()
    val syncing by viewModel.syncing.collectAsState()

    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var familyName by remember { mutableStateOf("") }
    var seniorName by remember { mutableStateOf("") }
    var checkEmailNotice by remember { mutableStateOf(false) }

    Column(
        verticalArrangement = Arrangement.spacedBy(16.dp),
        modifier = Modifier
            .fillMaxSize()
            .verticalScroll(rememberScrollState())
            .padding(24.dp),
    ) {
        Text(
            text = stringResource(R.string.account_title),
            style = MaterialTheme.typography.headlineMedium,
        )

        when (syncState) {
            is SyncManager.State.NotConfigured -> {
                Text(
                    text = stringResource(R.string.account_not_configured),
                    style = MaterialTheme.typography.bodyLarge,
                )
            }

            is SyncManager.State.Loading -> {
                CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                Text(
                    text = stringResource(R.string.account_loading),
                    style = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.align(Alignment.CenterHorizontally),
                )
            }

            is SyncManager.State.SignedOut -> {
                Text(
                    text = stringResource(R.string.account_signed_out_body),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (checkEmailNotice) {
                    Text(
                        text = stringResource(R.string.account_check_email),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                OutlinedTextField(
                    value = email,
                    onValueChange = { email = it },
                    label = { Text(stringResource(R.string.account_email)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Email),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = password,
                    onValueChange = { password = it },
                    label = { Text(stringResource(R.string.account_password)) },
                    keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Password),
                    visualTransformation = PasswordVisualTransformation(),
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                GiantButton(
                    label = stringResource(R.string.account_sign_in),
                    onClick = { viewModel.signIn(email, password) },
                )
                GiantButton(
                    label = stringResource(R.string.account_sign_up),
                    onClick = {
                        viewModel.signUp(email, password) { checkEmailNotice = true }
                    },
                    outlined = true,
                )
            }

            is SyncManager.State.NeedsFamily -> {
                Text(
                    text = stringResource(R.string.account_needs_family_title),
                    style = MaterialTheme.typography.titleLarge,
                )
                OutlinedTextField(
                    value = familyName,
                    onValueChange = { familyName = it },
                    label = { Text(stringResource(R.string.account_family_name)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                OutlinedTextField(
                    value = seniorName,
                    onValueChange = { seniorName = it },
                    label = { Text(stringResource(R.string.account_senior_name)) },
                    singleLine = true,
                    textStyle = MaterialTheme.typography.bodyLarge,
                    modifier = Modifier.fillMaxWidth(),
                )
                GiantButton(
                    label = stringResource(R.string.account_create_family),
                    onClick = {
                        if (familyName.isNotBlank() && seniorName.isNotBlank()) {
                            viewModel.createFamily(familyName, seniorName)
                        }
                    },
                )
                GiantButton(
                    label = stringResource(R.string.account_sign_out),
                    onClick = { viewModel.signOut() },
                    outlined = true,
                )
            }

            is SyncManager.State.Ready -> {
                Text(
                    text = stringResource(R.string.account_connected),
                    style = MaterialTheme.typography.bodyLarge,
                )
                if (syncing) {
                    Text(
                        text = stringResource(R.string.account_syncing),
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.primary,
                    )
                }
                GiantButton(
                    label = stringResource(R.string.account_sync_now),
                    onClick = { viewModel.syncNow() },
                )
                GiantButton(
                    label = stringResource(R.string.account_sign_out),
                    onClick = { viewModel.signOut() },
                    outlined = true,
                )
            }
        }

        syncError?.let { error ->
            Text(
                text = stringResource(R.string.account_error_prefix, error),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.error,
            )
        }

        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }
}
