package com.ada.mudra.features.whatsapp

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import com.ada.mudra.MudraViewModel
import com.ada.mudra.R
import com.ada.mudra.ui.components.ContactAvatar
import com.ada.mudra.ui.components.GiantButton
import com.ada.mudra.ui.theme.CallGreen
import com.ada.mudra.ui.theme.WhatsAppGreen

@Composable
fun WhatsAppConfirmScreen(
    viewModel: MudraViewModel,
    contactId: String?,
    onGoBack: () -> Unit,
    onGoHome: () -> Unit,
) {
    val contact = viewModel.contactById(contactId)
    val whatsAppInstalled = remember { viewModel.whatsAppProvider.isWhatsAppInstalled() }
    var launchFailed by remember { mutableStateOf(false) }

    if (contact == null) {
        LaunchedEffect(Unit) { onGoHome() }
        return
    }

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.spacedBy(20.dp),
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
    ) {
        Spacer(modifier = Modifier.weight(0.5f))

        ContactAvatar(
            name = contact.displayName,
            photoFile = viewModel.avatarFile(contact),
            contentDescription = stringResource(R.string.cd_contact_photo, contact.displayName),
            size = 144,
        )
        Text(
            text = stringResource(R.string.wa_confirm_title, contact.displayName),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )

        when {
            !whatsAppInstalled -> Text(
                text = stringResource(R.string.wa_missing),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            launchFailed -> Text(
                text = stringResource(R.string.wa_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
            else -> Text(
                text = stringResource(R.string.wa_confirm_body),
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        if (whatsAppInstalled) {
            GiantButton(
                label = stringResource(R.string.wa_open),
                containerColor = WhatsAppGreen,
                onClick = {
                    viewModel.whatsAppProvider.openTrustedChat(contact.whatsappNumber).fold(
                        onSuccess = {
                            launchFailed = false
                            onGoHome()
                        },
                        onFailure = { launchFailed = true },
                    )
                },
            )
        } else if (contact.canPhoneCall) {
            GiantButton(
                label = stringResource(R.string.wa_call_instead, contact.displayName),
                containerColor = CallGreen,
                onClick = {
                    viewModel.phoneCallProvider.openDialer(contact.phoneE164).fold(
                        onSuccess = { onGoHome() },
                        onFailure = { launchFailed = true },
                    )
                },
            )
        }
        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }
}
