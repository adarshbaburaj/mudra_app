package com.ada.mudra.features.calling

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

@Composable
fun CallConfirmScreen(
    viewModel: MudraViewModel,
    contactId: String?,
    onGoBack: () -> Unit,
    onGoHome: () -> Unit,
) {
    val contact = viewModel.contactById(contactId)
    var launchFailed by remember { mutableStateOf(false) }

    // The contact can disappear mid-flow if a caregiver deletes it; return to
    // the safe home screen instead of showing a broken confirm screen.
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
            text = stringResource(R.string.call_confirm_title, contact.displayName),
            style = MaterialTheme.typography.headlineMedium,
            textAlign = TextAlign.Center,
        )
        Text(
            text = contact.relationLabel,
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
        )

        if (launchFailed) {
            Text(
                text = stringResource(R.string.call_failed),
                style = MaterialTheme.typography.bodyLarge,
                color = MaterialTheme.colorScheme.error,
                textAlign = TextAlign.Center,
            )
        }

        Spacer(modifier = Modifier.weight(1f))

        GiantButton(
            label = stringResource(
                if (launchFailed) R.string.try_again else R.string.call_now
            ),
            containerColor = CallGreen,
            onClick = {
                viewModel.phoneCallProvider.openDialer(contact.phoneE164).fold(
                    onSuccess = {
                        launchFailed = false
                        onGoHome()
                    },
                    onFailure = { launchFailed = true },
                )
            },
        )
        GiantButton(
            label = stringResource(R.string.go_back),
            onClick = onGoBack,
            outlined = true,
        )
    }
}
